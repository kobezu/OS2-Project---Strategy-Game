class GameState(game: Game):
  var actingPlayer: Player = RedPlayer
  private val scoreToWin = 2

  def advanceRound() =
    RedPlayer.resources += 1
    BluePlayer.resources += 1
    game.gameLevel.troops.foreach(_.refresh())
    game.gameLevel.areas.foreach(_.updateControl())
    checkWinner().foreach(player => println(player.toString + " is winner!"))

  def advanceTurn() =
    if actingPlayer == RedPlayer then
      actingPlayer = BluePlayer
    else
      advanceRound()
      actingPlayer = RedPlayer

  def checkWinner(): Option[Player] =
    if BluePlayer.settlements >= scoreToWin | RedPlayer.baseCaptured then
      Some(BluePlayer)
    else if RedPlayer.settlements >= scoreToWin | BluePlayer.baseCaptured then
      Some(RedPlayer)
    else
      None
end GameState
