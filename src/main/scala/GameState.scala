class GameState():
  var actingPlayer: Player = Red
  //var gameEnd = false
  private val scoreToWin = 10

  def advanceTurn() =
    if actingPlayer == Red then
      actingPlayer = Blue
    else actingPlayer = Red
    println("turn advance")

  def checkWinner(): Option[Player] =
    if Blue.settlements >= scoreToWin then
      Some(Blue)
    else if Red.settlements >= scoreToWin then
      Some(Red)
    else
      None
end GameState
