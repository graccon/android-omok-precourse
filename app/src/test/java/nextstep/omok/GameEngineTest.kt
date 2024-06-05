package nextstep.omok

import android.content.Context
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class GameEngineTest {
    private lateinit var gameEngine: GameEngine
    private lateinit var gameView: GameView

    @BeforeEach
    fun setUp() {
        gameView = FakeGameView()
        gameEngine = GameEngine(gameView)
        gameEngine.startGame()
    }

    private val directions = listOf(
        Direction(1, 0),  // horizontal
        Direction(0, 1),  // vertical
        Direction(1, 1),  // diagonal down-right
        Direction(1, -1)  // diagonal down-left
    )
    fun getCurrentPlayer(): Player {
        return gameEngine.currentPlayer
    }

    fun getOtherPlayer(): Player {
        return when (getCurrentPlayer()) {
            gameEngine.blackPlayer -> gameEngine.whitePlayer
            else -> gameEngine.blackPlayer
        }
    }

    fun getBoardSize() : Int {
        return gameEngine.BOARD_SIZE
    }


    @Test
    fun `(startGame) initialize the game with black player`() {
        assertTrue(gameEngine.currentPlayer == gameEngine.blackPlayer)
    }

    @Test
    fun `(isDraw) return false when turn count is less than the maximum`() {
        val maxTurns = gameEngine.BOARD_SIZE * gameEngine.BOARD_SIZE
        repeat(maxTurns - 1) {
            if (it == 0) assertFalse(gameEngine.isDraw())
            gameEngine.onCellClick(FakeCell(0, 0)) // Simulate turns until one less than the maximum
        }
        assertFalse(gameEngine.isDraw()) // isDraw should be false if not yet reached the maximum
    }

    @Test
    fun `(isDraw) return true when turn count reaches the maximum`() {
        val maxTurns = gameEngine.BOARD_SIZE * gameEngine.BOARD_SIZE
        repeat(maxTurns) {
            gameEngine.onCellClick(FakeCell(0, 0)) // Simulate turns until reaching the maximum
        }
        assertTrue(gameEngine.isDraw()) // isDraw should be true when reached the maximum
    }

    @Test
    fun `(isSamePlayerStone) return true when the stone matches the current player's stone`() {
        val (row, col) = 7 to 7
        val currentPlayer = getCurrentPlayer()
        gameEngine.boardStatus[row][col] = currentPlayer.stone

        assertTrue(gameEngine.isSamePlayerStone(row, col))
    }

    @Test
    fun `(isSamePlayerStone) return false when the stone does not match the current player's stone`() {
        val (row, col) = 7 to 7
        gameEngine.boardStatus[row][col] = getOtherPlayer().stone

        assertFalse(gameEngine.isSamePlayerStone(row, col))
    }

    @Test
    fun `(countStones) return 0 when no stones are around`() {
        val (row, col) = 7 to 7
        gameEngine.boardStatus[row][col] = getCurrentPlayer().stone

        directions.forEach{ (dx, dy) ->
            assertEquals(gameEngine.countStones(row, col, dx, dy), 0)
        }
    }

    @Test
    fun `(countStones) count surrounding stones of the current player's color`() {
        val (row, col) = 7 to 7
        val currentPlayer = getCurrentPlayer()
        gameEngine.boardStatus[row][col] = currentPlayer.stone
        directions.forEach{ (dx, dy) ->
            gameEngine.boardStatus[row + dx][col + dy] = currentPlayer.stone
        }
        directions.forEach{ (dx, dy) ->
            assertEquals(gameEngine.countStones(row, col, dx, dy), 1)
        }
    }

    @Test
    fun `(countStones) not count opponent's stones around the current player's stone`() {
        val (row, col) = 7 to 7
        val currentPlayer = getCurrentPlayer()
        val otherPlayer = getOtherPlayer()
        gameEngine.boardStatus[row][col] = currentPlayer.stone
        directions.forEach{ (dx, dy) ->
            gameEngine.boardStatus[row + dx][col + dy] = otherPlayer.stone
        }
        directions.forEach{ (dx, dy) ->
            assertEquals(gameEngine.countStones(row, col, dx, dy), 0)
        }
    }

    @Test
    fun `(isWinningMove) Case1_countStones = 1`() {
        val (row, col) = 7 to 7
        gameEngine.boardStatus[row][col] = getCurrentPlayer().stone
        assertFalse(gameEngine.isWinningMove(FakeCell(row, col)))
    }

    fun printBoardStatus(boardStatus: Array<Array<Stone?>>) {
        for (row in boardStatus.indices) {
            for (col in boardStatus[row].indices) {
                when (boardStatus[row][col]) {
                    is BlackStone -> print("B ")
                    is WhiteStone -> print("W ")
                    else -> print("X ")
                }
            }
            println()
        }
    }

    fun changePlayer() {
        gameEngine.currentPlayer = gameEngine.whitePlayer
        // println("CurrentPlayer is " + getCurrentPlayer().name)
        // println("OtherPlayer is " + getOtherPlayer().name)
    }

    @Test
    fun `(isWinningMove) when count is exactly OMOK_COUNT`() {
        var (row, col) = 7 to 7
        changePlayer()
        val currentPlayer = getCurrentPlayer()

        gameEngine.boardStatus[row][col] = currentPlayer.stone

        directions.forEach{ (dx, dy) ->
            for (i in 0..gameEngine.OMOK_COUNT/2) {
                gameEngine.boardStatus[row + dx * i][col + dy * i] = currentPlayer.stone
                gameEngine.boardStatus[row + dx * -i][col + dy * -i] = currentPlayer.stone
            }
        }
        assertTrue(gameEngine.isWinningMove(FakeCell(row, col)))
    }

    @Test
    fun `(isWinningMove) when count is less than OMOK_COUNT`() {
        var (row, col) = 7 to 7
        changePlayer()
        val currentPlayer = getCurrentPlayer()

        gameEngine.boardStatus[row][col] = currentPlayer.stone

        directions.forEach{ (dx, dy) ->
            for (i in 0..gameEngine.OMOK_COUNT/2) {
                gameEngine.boardStatus[row + dx * i][col + dy * i] = currentPlayer.stone }
        }
        assertFalse(gameEngine.isWinningMove(FakeCell(row, col)))
    }

    @Test
    fun `(isWinningMove) when count is greater than OMOK_COUNT`() {
        var (row, col) = 7 to 7
        changePlayer()
        val currentPlayer = getCurrentPlayer()
        // 최악의 경우: 모든 방향으로 최대치의 순회
        repeat(gameEngine.BOARD_SIZE) { r ->
            repeat(gameEngine.BOARD_SIZE) { c ->
                gameEngine.boardStatus[r][c] = currentPlayer.stone
            }
        }
        assertTrue(gameEngine.isWinningMove(FakeCell(row, col)))
    }

    @Test
    fun `(isWithinBounds) return true when row and col are within bounds`() {
        val row = getBoardSize() - 3
        val col = getBoardSize() - 4
        assertTrue(gameEngine.isWithinBounds(row, col))
    }

    @Test
    fun `(isWithinBounds) return false when row is out of bounds`() {
        val row = getBoardSize() + 1
        val col = getBoardSize() - 4
        assertFalse(gameEngine.isWithinBounds(row, col))
    }

    @Test
    fun `(isWithinBounds) return false when col is out of bounds`() {
        val row = getBoardSize() - 3
        val col = getBoardSize()
        assertFalse(gameEngine.isWithinBounds(row, col))
    }

    @Test
    fun `(isWithinBounds) return false when both row and col are out of bounds`() {
        val row = -2
        val col = getBoardSize() + 6
        assertFalse(gameEngine.isWithinBounds(row, col))
    }

}


class FakeCell(val row : Int, val col : Int) : Cell {
    override var position: Pair<Int, Int>? = null
    var stone: Stone? = null

    init {
        position = row to col
    }

    override fun isEmpty(): Boolean {
        // FakeCell은 항상 비어있는 것으로 간주
        return true
    }
}

class FakeGameView : GameView {
    var isBoardInitialized = false
    var isPlayerFlagUpdated = false

    override fun initializeBoard() {
        isBoardInitialized = true
    }

    override fun updatePlayerFlag(player: Player) {
        isPlayerFlagUpdated = true
    }

    override fun displayEnding(message: String) {
        // Implementation not needed for this test
    }

    override fun onCellClick(cell: Cell) {
        TODO("Not yet implemented")
    }
}

