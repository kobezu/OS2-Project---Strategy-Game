import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.text.Font
import Stat.*

class MenuElement(val name: String, elementCoords: (Double, Double), width: Int, height: Int):
  def tryClickMenuElement(clickCoords: (Double, Double)): Boolean =
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
      x = xPos + xn * elementWidth
      y = yPos + yn * elementHeight
      fill = bgColor
    val text = new Text(name):
      font = new Font(20)
      fill = color
      x = bg.getX + (elementWidth/3.0).toInt
      y = bg.getY + (elementHeight/1.5).toInt
    this.children += bg
    this.children += text
    text

  def createMenuElement(n: Int, name: String) =
    createTxtElement(0, n, name, txtColor)
    MenuElement(name, (xPos, yPos + n * elementHeight), elementWidth, elementHeight)


object TroopMenu extends UI(1000, 200):
  val elementWidth = 200
  val elementHeight = 70
  val bgColor = Gray
  val txtColor = White
  val menuElements = Vector(createMenuElement(0, "Move"), createMenuElement(1, "Attack"), createMenuElement(2, "Wait"))
  val textElements = Vector()

object TroopAttackMenu extends UI(1000, 200):
  val elementWidth = 200
  val elementHeight = 70
  val bgColor = Gray
  val txtColor = White
  val menuElements = Vector(createMenuElement(0, "Attack"), createMenuElement(1, "Wait"))
  val textElements = Vector()

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
  val elementHeight = 50
  val bgColor = White
  val txtColor = Black
  val menuElements = Vector()

class TroopInfo(xPos: Int, yPos: Int, troop: Troop) extends InfoUI(xPos, yPos):
  val elementWidth = 100
  val textElements =
    Vector(statInfo(0, Hp), statInfo(1, Atk), statInfo(2, Def), statInfo(3, Mov), statInfo(4, Rng))

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
  val textElements = Vector(createTxtElement(0, 0, "Terrain: " + tile.id, txtColor))

class AreaInfo(xPos: Int, yPos: Int, area: Area) extends InfoUI(xPos, yPos):
  val elementWidth = 200
  val textElements = Vector(createTxtElement(0, 0, "Area Strength: " + area.strength, txtColor))