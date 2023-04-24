import javafx.application.Platform
import javafx.scene.layout.Background
import scalafx.scene.paint.Color.*
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.text.Text
import scalafx.scene.text.Font
import javafx.scene.text.TextAlignment
import scala.concurrent.Future
import java.awt.*
import java.awt.Dimension
import javafx.scene.layout.StackPane

import concurrent.ExecutionContext.Implicits.global

object GameApp extends JFXApp3:
  val fileManager = FileManager()
  var loadedGame: Option[Game] = None
  val screenSize: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  //save game when closing GameApp
  override def stopApp() =
    if loadedGame.nonEmpty then
      if loadedGame.get.gameState.winner.isEmpty then
        loadedGame.foreach(fileManager.saveGame(_))
      else fileManager.clearSave("saveFile.xml")

  def start(): Unit =
    val menuWidth = 500
    val menuHeight = 600
    stage = new JFXApp3.PrimaryStage:
      width = menuWidth
      height = menuHeight
      resizable = false

    //loading screen
    val loadingText = new Text("Loading..."):
      font = new Font(50)
      fill = White
    loadingText.setTextAlignment(TextAlignment.CENTER)
    val stack = StackPane(loadingText)
    val loadingScreen = Scene(stack, Black)

    def startGame(fileName: String) =
        val tileRes = 24
        val tileInfoArea = 48
        val gameInfoArea = 200
        stage.scene = loadingScreen
        val loadingGame: Future[Game] = Future{fileManager.loadSave(fileName)}
        loadingGame.onComplete(result =>
          Platform.runLater(() => {
            val game = result.get
            val width = tileRes * game.gameLevel.gridWidth + gameInfoArea + 16
            val height = tileRes * game.gameLevel.gridHeight + tileInfoArea + 39
            stage.setWidth(width)
            stage.setHeight(height)
            stage.centerOnScreen()
            stage.scene = GameScene(Pane(), game, width, height)
            loadedGame = Some(game)
          }))

    //new game
    val root_newGame = Pane()
    val newGameScreen = Scene(parent = root_newGame)
    {
      root_newGame.background = Background.fill(LightCyan)
      val players = Players(92, menuHeight-550)
      root_newGame.children += players
      val startGameBtn = StartGameBtn((menuWidth / 2 - 100), menuHeight - 250)
      root_newGame.children += startGameBtn

      def handleInput() =
        root_newGame.onMouseClicked = event => {
          val x = event.getX
          val y = event.getY

          def isCPU(player: Player) =
            players.playerOrCPU(player).getText == "CPU"

          players.menuElements.zipWithIndex.find(_._1.tryClickMenuElement((x,y))) match
            case Some(menuElement) => if menuElement._2 == 0 then players.change(RedPlayer) else players.change(BluePlayer)
            case None =>
              if startGameBtn.menuElements.exists(_.tryClickMenuElement((x, y))) then
                val file = "newGame_1.xml"
                fileManager.setPlayersControl(file, isCPU(RedPlayer), isCPU(BluePlayer))
                startGame(file)
        }
      handleInput()
    }

    //main menu
    val root_menu = Pane()
    val mainMenu = Scene(parent = root_menu)
    {
      root_menu.background = Background.fill(LightCyan)

      val mainMenu = MainMenu((menuWidth / 2 - 200/2), (menuHeight / 2 - 2*70))
      root_menu.children += mainMenu

      def handleInput() =
        root_menu.onMouseClicked = event => {
          val x = event.getX
          val y = event.getY
          mainMenu.menuElements.find(_.tryClickMenuElement((x, y))) match
            case Some(menuElement) => menuElement.name match
              case "New Game" => stage.scene = newGameScreen
              case "Load Game" =>
                if fileManager.nonEmptySave("saveFile.xml") then startGame("saveFile.xml")
            case None =>
        }

      handleInput()
    }

    stage.scene = mainMenu

end GameApp
