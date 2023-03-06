import javafx.scene.paint.Color

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.text.Font
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle

object GameApp extends JFXApp3:
  val game = Game()
  //resolution of tile
  val tileRes = 48
  //convert scene coords to grid coords
  def sceneToGridCoords(x:Double, y: Double): (Int, Int) = ((x/tileRes).toInt, (y/tileRes).toInt)
  //convert grid coords to scene coords
  def gridToSceneCoordX(gridCoords: (Int, Int)) = gridCoords._1 * tileRes
  def gridToSceneCoordY(gridCoords: (Int, Int)) = gridCoords._2 * tileRes
  //get tile and troop images
  def getTileImage(tileId: String) = Image(FileInputStream("images\\tiles\\" + tileId + ".png"))
  def getTroopImage(troopId: String) = Image(FileInputStream("images\\troops\\" + troopId + ".png"))

  def start(): Unit =
    stage = new JFXApp3.PrimaryStage:
      title = "Game App"
      //makes window size equal to grid size
      width = tileRes * game.gameLevel.gridWidth + 16
      height = tileRes * game.gameLevel.gridHeight + 39

    val root = Pane()
    val scene = Scene(parent = root)
    stage.scene = scene

    //initialize branches of root
    val tiles = Pane()
    val troops = Pane()
    val userInterface = Pane()
    val highlights = Pane()
    root.children += tiles
    root.children += highlights
    root.children += troops
    root.children += userInterface

    //highlights tile in given grid coords
    def highlightTile(gridCoords: (Int, Int), color: Color) =
      val rectangle = new Rectangle:
        x = gridToSceneCoordX(gridCoords)
        y = gridToSceneCoordY(gridCoords)
        width = tileRes
        height = tileRes
        fill = color.opacity(0.3)
      highlights.children += rectangle

    def renderGameLevel() =
      //render tiles in gameLevel
      var currentX = 0
      var currentY = 0
      for row <- game.gameLevel.tileGrid do
        for tile <- row do
          val imageView = new ImageView()
          imageView.setImage(getTileImage(tile.id))
          imageView.setX(currentX)
          imageView.setY(currentY)
          tiles.children += imageView
          currentX += tileRes
        currentY += tileRes
        currentX = 0

      //render troops in gameLevel
      for troop <- game.gameLevel.troops do
        game.gameLevel.tileAt(troop.gridCoords).moveTo(troop) // saves troop to tile at it's grid coordinates
        val imageView = new ImageView()
        imageView.setImage(getTroopImage(troop.id))
        imageView.setX(gridToSceneCoordX(troop.gridCoords))
        imageView.setY(gridToSceneCoordY(troop.gridCoords))
        troop.imageViewIndex = troops.children.size //saves troop's imageView-index
        troops.children += imageView
    end renderGameLevel

    //listens for mouse clicks and then does things according to the mouse position and what is current action
    def handleInput() =
      root.onMouseClicked = event => {
        val gridCoords = sceneToGridCoords(event.getX, event.getY)
        val clickedTile = game.gameLevel.tileAt(gridCoords)
        game.currentAction match
          //makes clicked tile the focus
          case NoFocus() | TileFocus(_) =>
            highlights.children.clear()
            highlightTile(gridCoords, Black)
            if clickedTile.troop.nonEmpty then
              userInterface.children += TroopMenu
              game.currentAction = TroopFocus(clickedTile)
            else
              game.currentAction = TileFocus(clickedTile)

          //changes current action based on what menu element was clicked
          case TroopFocus(activeTile) =>
            val activeTroop = activeTile.troop.get
            TroopMenu.menuElements.find(_.tryClickMenuElement((event.getX, event.getY))) match
              case Some(menuElement) =>
                menuElement.name match
                  case "Move" =>
                    game.currentAction = Moving(activeTile)
                    troopMoveRange(activeTroop).foreach(highlightTile(_, LightBlue))
                  case "Attack" =>
                    println("attack")
                    removeFocus()
                  case "Wait" =>
                    removeFocus()
                    println("wait")
              case None => removeFocus()
              userInterface.children.remove(0)

          //moves active troop if the tile clicked is legal for movement
          case Moving(activeTile) =>
            removeFocus()
            val movingTroop = activeTile.troop.get
            if troopMoveRange(movingTroop).contains(gridCoords) && clickedTile.isPassable then
              movingTroop.move(gridCoords) //updates moving troop's coords
              clickedTile.moveTo(movingTroop) //stores troop to the new tile
              activeTile.moveFrom() //removes troop from the old tile
              //renders troop's position change
              troops.children(movingTroop.imageViewIndex)
                .relocate(gridToSceneCoordX(movingTroop.gridCoords), gridToSceneCoordY(movingTroop.gridCoords))
      }
      def removeFocus() =
        highlights.children.clear()
        game.currentAction = NoFocus()

      def troopMoveRange(troop: Troop) =
        game.gameLevel.coordsAtRange(troop.gridCoords, troop.movement)
    end handleInput


    renderGameLevel()
    handleInput()

end GameApp
