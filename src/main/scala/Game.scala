class Game():
  //val generatedMap = Vector.fill(20, 40)(scala.util.Random.nextInt(3))
  val fileManager = FileManager(this)
  val gameLevel = GameLevel(fileManager.gameMapReader("test.txt"))
  val gameState = GameState(this)
  var currentAction: Action = NoFocus()
end Game

