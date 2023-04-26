import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.text.Font
import Stat.*
import javafx.scene.layout.StackPane

class MenuElement(val name: String, elementCoords: (Double, Double), width: Int, height: Int):
  def tryClick(clickCoords: (Double, Double)): Boolean =
    clickCoords._1 >= elementCoords._1 && clickCoords._1 <= elementCoords._1 + width &&
      clickCoords._2 >= elementCoords._2 && clickCoords._2 <= elementCoords._2 + height

abstract class UI(xPos: Int, yPos: Int) extends Pane:
  val elementWidth: Int
  val elementHeight: Int
  val bgColor: Color
  val txtColor: Color
  val menuElements: Vector[MenuElement]
  val textElements: Vector[Text]

  def createTxtElement(xn: Int, yn: Int, name: String, color: Color) =
    val bg = new Rectangle:
      width = elementWidth
      height = elementHeight
      fill = bgColor
    val text = new Text(name):
      font = new Font(20)
      fill = color
    val stack = new StackPane()
    stack.setLayoutX(xPos + xn * elementWidth)
    stack.setLayoutY(yPos + yn * elementHeight)
    stack.getChildren.addAll(bg, text)
    //this.children += bg
    this.children += stack
    text

  def createMenuElement(n: Int, name: String) =
    createTxtElement(0, n, name, txtColor)
    MenuElement(name, (xPos, yPos + n * elementHeight), elementWidth, elementHeight)

abstract class Menu(xPos: Int, yPos: Int) extends UI(xPos, yPos):
  val elementWidth = 200
  val elementHeight = 70
  val bgColor = Gray
  val txtColor = White
  val textElements = Vector()

//NewGame
class Players(x: Int, y: Int) extends UI(x, y):
  val elementWidth = 100
  val elementHeight = 50
  val bgColor = Gray
  val txtColor = White
  val menuElements = Vector(createMenuElement(0, "Change"), createMenuElement(2, "Change"))
  val textElements = Vector(createTxtElement(2, 0, "Player", Red), createTxtElement(2, 2, "Player", Blue))

  def change(player: Player) =
    val text = playerOrCPU(player)
    text.getText match
      case "Player" => text.setText("CPU")
      case "CPU" => text.setText("Player")
  
  def playerOrCPU(player: Player) =
    player match
      case RedPlayer => textElements(0)
      case BluePlayer => textElements(1)

class StartGameBtn(x: Int, y: Int) extends Menu(x, y):
  val menuElements = Vector(createMenuElement(0, "Start Game"))

//MainMenu
class MainMenu(x: Int, y: Int) extends Menu(x, y):
  val menuElements = Vector(createMenuElement(0, "New Game"), createMenuElement(2, "Load Game"))

//GameScene
object TroopMenu extends Menu(1000, 200):
  val menuElements = Vector(createMenuElement(0, "Move"), createMenuElement(1, "Attack"), createMenuElement(2, "Wait"))

object TroopAttackMenu extends Menu(1000, 200):
  val menuElements = Vector(createMenuElement(0, "Attack"), createMenuElement(1, "Wait"))

object BuildMenu extends Menu(1000, 200):
  val menuElements =
    Vector(createMenuElement(0, "Solider"), createMenuElement(1, "Sniper"),
      createMenuElement(2, "Tank"), createMenuElement(3, "Artillery"), createMenuElement(4, "Apache"))

class AdvanceTurnBtn(xPos: Int, yPos: Int) extends UI(xPos, yPos):
  val elementWidth = 300
  val elementHeight = 50
  val bgColor = Gray
  val txtColor = White
  val menuElements = Vector(createMenuElement(0, "Advance Turn"))
  val textElements = Vector()

class TurnSign(xPos: Int, yPos: Int, actingPlayer: Player) extends UI(xPos, yPos):
  val elementWidth = 300
  val elementHeight = 50
  val bgColor = Gray
  val txtColor = White
  val menuElements = Vector()
  val textElements = Vector(createTxtElement(0, 0, "Turn: " + actingPlayer, txtColor))

  def updateTurn(actingPlayer: String) =
    textElements.foreach(_.setText("Turn: " + actingPlayer))

abstract class InfoUI(xPos: Int, yPos: Int) extends UI(xPos, yPos):
  val elementHeight = 48
  val bgColor = LightCyan
  val txtColor = Black
  val menuElements = Vector()

class TroopInfo(xPos: Int, yPos: Int, troop: Troop) extends InfoUI(xPos, yPos):
  val elementWidth = 100
  val textElements = Vector(statInfo(0, Hp), statInfo(1, Atk), statInfo(2, Def), statInfo(3, Mov), statInfo(4, Rng))

  def statInfo(n: Int, stat: Stat) =
    val statDifference = troop.stats(stat) - troop.baseStat(stat)
    //set color based on how stat is modified
    val color ={
      if statDifference == 0 then txtColor
      else if statDifference > 0 then Blue
      else Red}
    createTxtElement(n, 0, stat.toString + ": " + troop.stats(stat), color)

class TileInfo(xPos: Int, yPos: Int, tile: Tile) extends InfoUI(xPos, yPos):
  val elementWidth = 200
  val textElements = tile.troop match
    case Some(troop) => Vector(createTxtElement(0, 0, "Terrain: " + tile.id, txtColor)) ++ TroopInfo(xPos-elementWidth, yPos, troop).textElements
    case None => Vector(createTxtElement(0, 0, "Terrain: " + tile.id, txtColor))

class AreaInfo(xPos: Int, yPos: Int, area: Area) extends InfoUI(xPos, yPos):
  val elementWidth = 200
  val textElements = Vector(createTxtElement(0, 0, "Area Strength: " + area.strength, txtColor))

class GameInfo(xPos: Int, yPos: Int, actingPlayer: Player) extends InfoUI(xPos, yPos):
  val elementWidth = 200
  val textElements = Vector(createTxtElement(0, 0, "Turn: " + actingPlayer, txtColor))
    ++ playerInfo(1, RedPlayer) ++ playerInfo(3, BluePlayer)

  def playerInfo(n: Int, player: Player) =
    Vector(createTxtElement(0, 0 + n, "Score: " + player.settlements, player.color),
    createTxtElement(0, 1 + n, "Resources: " + player.resources, player.color))

  def updateResources(player: Player) =
    val index = if player == RedPlayer then 2 else 4
    textElements(index).setText("Resources: " + player.resources)
    
  def updateTurn(actingPlayer: String) =
    textElements(0).setText("Turn: " + actingPlayer)
    textElements(1).setText("Score: " + RedPlayer.settlements)
    textElements(3).setText("Score: " + BluePlayer.settlements)
    updateResources(RedPlayer)
    updateResources(BluePlayer)