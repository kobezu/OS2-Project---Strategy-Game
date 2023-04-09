class Game():
  val fileManager = FileManager(this)
  val gameLevel = GameLevel(fileManager.gameMapReader("test.txt"))
  val gameState = GameState(this)
  var currentAction: Action = NoFocus()
end Game

