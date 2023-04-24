class GameState(game: Game):
  var actingPlayer: Player = RedPlayer
  var winner: Option[Player] = None
  private val scoreToWin = 6

  def advanceRound() =
    RedPlayer.resources += 1
    BluePlayer.resources += 1
    game.gameLevel.troops.foreach(_.refresh())
    game.gameLevel.areas.foreach(_.updateControl())
    checkWinner()

  def advanceTurn() =
    if actingPlayer == RedPlayer then
      actingPlayer = BluePlayer
    else
      advanceRound()
      actingPlayer = RedPlayer

  def checkWinner() =
    if BluePlayer.settlements >= scoreToWin | RedPlayer.baseCaptured then
      winner = Some(BluePlayer)
    else if RedPlayer.settlements >= scoreToWin | BluePlayer.baseCaptured then
      winner = Some(RedPlayer)

end GameState
