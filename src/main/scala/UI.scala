import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.text.Font

class MenuElement(val name: String, elementCoords: (Double, Double), width: Int, height: Int):
  def tryClickMenuElement(clickCoords: (Double, Double)): Boolean =
    clickCoords._1 >= elementCoords._1 && clickCoords._1 <= elementCoords._1 + width &&
      clickCoords._2 >= elementCoords._2 && clickCoords._2 <= elementCoords._2 + height

abstract class UI(xPos: Int, yPos: Int) extends Pane:
  val elementWidth: Int
  val elementHeight: Int
  val menuElements: Vector[MenuElement]
  def createMenuElement(n: Int, name: String) =
    val bg = new Rectangle:
      width = elementWidth
      height = elementHeight
      x = xPos
      y = yPos + n * elementHeight
      fill = Gray
    val text = new Text(name):
      font = new Font(20)
      fill = White
      x = bg.getX + 75
      y = bg.getY + 45
    this.children += bg
    this.children += text
    MenuElement(name, (xPos, yPos + n * elementHeight), elementWidth, elementHeight)

object TroopMenu extends UI(1000, 200):
  val elementWidth = 200
  val elementHeight = 70
  val menuElements = Vector(createMenuElement(0, "Move"), createMenuElement(1, "Attack"), createMenuElement(2, "Wait"))

object TroopAttackMenu extends UI(1000, 200):
  val elementWidth = 200
  val elementHeight = 70
  val menuElements = Vector(createMenuElement(0, "Attack"), createMenuElement(1, "Wait"))

class AdvanceTurn(xPos: Int, yPos: Int) extends UI(xPos, yPos):
  val elementWidth = 300
  val elementHeight = 50
  val menuElements = Vector(createMenuElement(0, "Advance Turn"))