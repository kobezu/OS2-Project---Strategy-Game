import scala.collection.mutable.Buffer
import javafx.scene.paint.Color
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.text.Font
import scalafx.scene.text.Text
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle

object GameApp extends JFXApp3:
  val game = Game()
  //resolution of tile
  val tileRes = 48
  val infoArea = 50
  //convert scene coords to grid coords
  def sceneToGridCoords(x:Double, y: Double): (Int, Int) = ((x/tileRes).toInt, (y/tileRes).toInt)
  //convert grid coords to scene coords
  def gridToSceneCoordX(gridCoords: (Int, Int)) = gridCoords._1 * tileRes
  def gridToSceneCoordY(gridCoords: (Int, Int)) = gridCoords._2 * tileRes
  //get tile and troop images
  def getTileImage(tileId: String) = Image(FileInputStream("data\\images\\tiles\\" + tileId + ".png"))
  def getTroopImage(troop: Troop) = Image(FileInputStream("data\\images\\troops\\" + troop.id + "_" + troop.controller.toString.toLowerCase + ".png"))

  def start(): Unit =
    val mapWidth = tileRes * game.gameLevel.gridWidth
    val mapHeight = tileRes * game.gameLevel.gridHeight

    stage = new JFXApp3.PrimaryStage:
      title = "Game App"
      //makes window size equal to grid size
      width = mapWidth + 16
      height = mapHeight + infoArea + 39

    val root = Pane()
    val scene = Scene(parent = root)
    stage.scene = scene

    //initialize branches of root
    val tiles = Pane()
    val troops = Pane()
    val userInterface = Pane()
    val highlights = Pane()
    root.children += tiles
    root.children += troops
    root.children += highlights
    root.children += userInterface
    //initialize branches of highlight
    val focusHL = Pane()
    val areaHL = Pane()
    highlights.children += focusHL
    highlights.children += areaHL
    //initialize branches of userInterface
    val menuUI = Pane()
    val staticUI = Pane()
    val infoUI = Pane()
    userInterface.children += menuUI
    userInterface.children += staticUI
    userInterface.children += infoUI
    //add static UI-elements
    val advanceTurnBtn = AdvanceTurnBtn(mapWidth-300, mapHeight)
    var turnSign: TurnSign = TurnSign(0, mapHeight, game.gameState.actingPlayer)
    staticUI.children += advanceTurnBtn
    staticUI.children += turnSign

    //highlights tile in given grid coords
    def highlightTile(gridCoords: (Int, Int), color: Color) =
      val rectangle = new Rectangle:
        x = gridToSceneCoordX(gridCoords)
        y = gridToSceneCoordY(gridCoords)
        width = tileRes
        height = tileRes
        fill = color.opacity(0.4)
      focusHL.children += rectangle

    //outlines area
    def outlineArea(gridCoords: Vector[(Int, Int)], color: Color) =
      val minX = gridCoords.map(_._1).min
      val maxX = gridCoords.map(_._1).max
      val minY = gridCoords.map(_._2).min
      val maxY = gridCoords.map(_._2).max
      val lineWidth = 5

      def drawVerticalLine(sceneX: Int, sceneY: Int) =
        val rectangle = new Rectangle:
          x = sceneX
          y = sceneY
          width = lineWidth
          height = tileRes
          fill = color
        rectangle

      def drawHorizontalLine(sceneX: Int, sceneY: Int) =
        val rectangle = new Rectangle:
          x = sceneX
          y = sceneY
          width = tileRes
          height = lineWidth
          fill = color
        rectangle

      for coords <- gridCoords do
        val x = coords._1
        val y = coords._2
        if x == minX then
          areaHL.children += drawVerticalLine(gridToSceneCoordX(coords), gridToSceneCoordY(coords))
        if x == maxX then
          areaHL.children += drawVerticalLine(gridToSceneCoordX(coords) + tileRes - lineWidth, gridToSceneCoordY(coords))
        if y == minY then
          areaHL.children += drawHorizontalLine(gridToSceneCoordX(coords), gridToSceneCoordY(coords))
        if y == maxY then
          areaHL.children += drawHorizontalLine(gridToSceneCoordX(coords), gridToSceneCoordY(coords) + tileRes - lineWidth)

    //refreshes all troop imageView-objects
    def refreshTroopImages() =
      troops.children.clear()
      for troop <- game.gameLevel.troops do
        val imageView = new ImageView()
        imageView.setImage(getTroopImage(troop))
        imageView.setX(gridToSceneCoordX(troop.gridCoords))
        imageView.setY(gridToSceneCoordY(troop.gridCoords))
        troop.imageViewIndex = troops.children.size //saves troop's imageView-index
        troops.children += imageView

    //updates color of area lines according to controller
    def updateLines() =
      for area <- game.gameLevel.areas do
        val controlColor =
          area.controller match
            case Some(player: Player) => player.color
            case None => Gray
        outlineArea(area.tiles.map(_.coords), controlColor)

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
        refreshTroopImages()

      //outline areas
      updateLines()

      //movement-range test
      /*def paintText(gridCoords: (Int, Int), textToPaint: String) =
        val text = new Text(textToPaint):
          font = new Font(20)
          fill = Black
          x = gridToSceneCoordX(gridCoords)
          y = gridToSceneCoordY(gridCoords) +20
        tiles.children += text

      val startTile = game.gameLevel.tileAt((18,18))
      game.gameLevel.movementRange((startTile, 0), 5, Buffer[(Tile, Int)]()).foreach(a => paintText(a._1.coords, a._2.toString))
      */
    end renderGameLevel

    //listens for mouse clicks and then does things according to the mouse position and what is current action
    def handleInput() =
      root.onMouseClicked = event => {
        //check if click was inside map
        if event.getY < mapHeight then
          val gridCoords = sceneToGridCoords(event.getX, event.getY)
          val clickedTile = game.gameLevel.tileAt(gridCoords)
          game.currentAction match
            //makes clicked tile the focus
            case NoFocus() | TileFocus(_) =>
              focusHL.children.clear()
              infoUI.children.clear()
              highlightTile(gridCoords, Black)
              infoUI.children += TileInfo(mapWidth - 600, mapHeight, clickedTile)
              //display area-info if clicked tile is in area
              game.gameLevel.areas.find(_.tiles.contains(clickedTile)) match
                case Some(area: Area) => infoUI.children += AreaInfo(mapWidth - 800, mapHeight, area)
                case None =>
              val clickedTroop = clickedTile.troop
              //check if clicked tile contains non-exhausted troop acting player controls
              if clickedTroop.nonEmpty then
                infoUI.children += TroopInfo(300, mapHeight, clickedTroop.get)
                if !clickedTroop.get.exhausted && clickedTroop.get.controller == game.gameState.actingPlayer then
                  game.currentAction = TroopFocus(clickedTile)
                  if clickedTroop.get.hasMoved then
                    addPopUp(TroopAttackMenu)
                  else
                    addPopUp(TroopMenu)
              else
                game.currentAction = TileFocus(clickedTile)

            //changes current action based on what menu element was clicked
            case TroopFocus(activeTile) =>
              val activeTroop = activeTile.troop.get
              val currentUI =
                if activeTroop.hasMoved then
                  TroopAttackMenu
                else TroopMenu
              currentUI.menuElements.find(_.tryClickMenuElement((event.getX, event.getY))) match
                case Some(menuElement) =>
                  menuUI.children.clear()
                  menuElement.name match
                    case "Move" =>
                      game.currentAction = Moving(activeTile)
                      troopMoveRange(activeTile).filter(_._1 != activeTile).foreach(a =>
                        val highlightColor = if a._1.isPassable then LightBlue else Red
                        highlightTile(a._1.coords, highlightColor))
                    case "Attack" =>
                      game.currentAction = Attacking(activeTile)
                      troopAttackRange(activeTroop).foreach(highlightTile(_, Red))
                    case "Wait" =>
                      removeFocus()
                      activeTroop.exhaust()
                case None => removeFocus()

            //moves active troop if the tile clicked is legal for movement
            case Moving(activeTile) =>
              val movingTroop = activeTile.troop.get
              if troopMoveRange(activeTile).exists(_._1.coords == gridCoords) && clickedTile.isPassable then
                movingTroop.move(gridCoords) //updates moving troop's coords
                clickedTile.moveTo(movingTroop) //stores troop to the new tile
                activeTile.removeTroop() //removes troop from the old tile
                //renders troop's position change
                troops.children(movingTroop.imageViewIndex)
                  .relocate(gridToSceneCoordX(movingTroop.gridCoords), gridToSceneCoordY(movingTroop.gridCoords))
              removeFocus()

            case Attacking(activeTile) =>
              val attackingTroop = activeTile.troop.get
              val target = clickedTile.troop
              //check if clicked tile is in range and contains enemy troop
              if troopAttackRange(attackingTroop).contains(gridCoords) && target.nonEmpty && target.get.controller != game.gameState.actingPlayer then
                attackingTroop.attack(target.get)
                if target.get.hp <= 0 then
                  removeTroop(target.get, clickedTile)
              removeFocus()
        else
          //check if advance turn was clicked
          if advanceTurnBtn.menuElements.exists(_.tryClickMenuElement((event.getX, event.getY))) then
            game.gameState.advanceTurn()
            turnSign.updateTurn(game.gameState.actingPlayer.toString)
            removeFocus()
            updateLines()
        }
      def removeFocus() =
        focusHL.children.clear()
        menuUI.children.clear()
        infoUI.children.clear()
        game.currentAction = NoFocus()

      def addPopUp(menu: UI) =
        menuUI.children.clear()
        menuUI.children += menu

      def troopMoveRange(tile: Tile) =
        game.gameLevel.tilesAtMovementRange(tile, tile.troop.get.movement)

      def troopAttackRange(troop: Troop) =
        game.gameLevel.coordsAtRange(troop.gridCoords, troop.range)

      //remove troop from game
      def removeTroop(troop: Troop, tile: Tile) =
        game.gameLevel.troops.remove(game.gameLevel.troops.indexOf(troop))
        tile.removeTroop()
        refreshTroopImages()
    end handleInput


    renderGameLevel()
    handleInput()

end GameApp
