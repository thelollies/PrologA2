% Outlining valid tanks
:-dynamic tank/2.
tank(red, City):- (road(City, _, _); road(_, City, _)), !.
tank(black, City):- (road(City, _, _); road(_, City, _)), !.

% True if TanksMoved is equivalent to Tanks after the tank at From
% is moved to To AND: 
% i) From and To are adjacent
% ii) There is a tank at From
% iii) There is no tank at To,
moveToPoint(From, To, Tanks, TanksMoved):-
	once(adjacent(From, To)),
	(member(tank(Team, From), Tanks) -> true),
	not(member(tank(_, To), Tanks)),
	delete(Tanks, tank(Team, From), FromRemoved),
	TanksMoved = [tank(Team, To)|FromRemoved].

% True if Tanks is equivalent to VictimRemoved the tank in At is removed AND:
% i) From and At are adjacent
% ii) There is a tank at From
% iii) There is a tank at At
% iv) The tanks at From and At are in different teams
fireOnPoint(From, At, Tanks, VictimRemoved):-
	once(adjacent(From, At)),
	member(tank(ShooterTeam, From), Tanks), !,
	member(tank(VictimTeam, At), Tanks), !,
	dif(ShooterTeam, VictimTeam),
	delete(Tanks, tank(VictimTeam, At), VictimRemoved).

% True if From is adjacent to Adjacent (there is a road between them).
adjacent(From, Adjacent):-
	road(From, Adjacent, _); road(Adjacent, From, _).

% True if there is a road between City1 and City2 with the cost Cost 
% It only searches for one cost as there should only be
% one street between two cities, therefore the cut is only for performance.
cost(City1, City2, Cost):-
	(road(City1, City2, Cost); road(City2, City1, Cost)), !.

% Gives all the possible moves for a tank to an Adjacent city with specified Energy.
% Calculates the updated  tank list, new tank location, move taken and energy remaining.
move(TankLocation, Adjacent, TankList, Move, Energy, EnergyAfter):- 
	Energy >= 25, 
	fireOnPoint(TankLocation, Adjacent, TankList, NewTankList), 
	Move = (fireAt(TankLocation, Adjacent), TankLocation, NewTankList),
	EnergyAfter is Energy - 25;
	cost(TankLocation, Adjacent, Cost),
	Energy >= Cost,
	moveToPoint(TankLocation, Adjacent, TankList, NewTankList),
	Move = (moveTo(TankLocation, Adjacent), Adjacent, NewTankList),
	EnergyAfter is Energy - Cost.

% True if Move is the best list of moves for the tank in TankList at TankLocation
% with Killable tanks that can be killed in one turn.
tanksKillable(TankLocation, TankList, Move, Killable):-
	findall(Moves, possibleMoves(TankLocation, TankList, Moves, 100),Results),
	bestMove(Results, Move, Killable).

% True if Move is the best move in Moves (in terms of number of tanks killed)
% such that the number of tanks killed equals Killable.
% Catches case where no moves are possible (apart from empty move)
bestMove([], [], 0):- !.
bestMove(Moves, Move, Killable):- bestMove_(Moves, Move, Killable).
bestMove_(Moves, Move, Killable):-
	bestMove_(Moves, [], Move, 0, Killable).
bestMove_([], BestSoFar, BestSoFar, BestKillable, BestKillable).
bestMove_([First|Rest], BestSoFar, BestMove, BestKillable, Killable):- 
	aggregate_all(count, member(fireAt(_, _), First), FirstKillable),
	(FirstKillable >= BestKillable -> 
		(NewBestMove = First, NewBestKillable = FirstKillable);
		(NewBestMove = BestSoFar, NewBestKillable = BestKillable)),
	bestMove_(Rest, NewBestMove, BestMove, NewBestKillable, Killable).

% True if NewTankList is equal to TankList after performing the best set
% of moves possible for the Team
doBestMoveForTeam(Team, TankList, NewTankList):-
	findall(City, member(tank(Team, City), TankList), Positions),
	mapTanksKillable(Positions, TankList, MoveList),
	bestMove(MoveList, BestMove, _),
	doMoveList(BestMove, TankList, NewTankList).

% True if the TankList is equivalent ot the MovedTankList
% with the list of moves performed on TankList
doMoveList([], NewTankList, NewTankList):- !.
doMoveList([Move|Rest], TankList, NewTankList):-
	doMove(Move, TankList, MovedTankList),
	doMoveList(Rest, MovedTankList, NewTankList).

% True if the UpdatedList is equivalent to the TankList after the
% specified move is undertaken (fireAt\2 or moveTo\2)
doMove(fireAt(_, Victim), TankList, UpdatedList):-
	delete(TankList, tank(_, Victim), UpdatedList).
doMove(moveTo(From, To), TankList, [tank(Team, To)|Removed]):-
	member(tank(Team, From), TankList), !,
	delete(TankList, tank(Team, From), Removed).

% maps the tanksKillable predicate to a list of cities
% which gives a list of the best moves from each city
mapTanksKillable([], _, []):- !.
mapTanksKillable([TankLocation|Positions], TankList, [Move| Others]):-
	tanksKillable(TankLocation, TankList, Move, _),
	mapTanksKillable(Positions, TankList, Others).

% True if there are no tanks left which are not in Team (no enemy tanks remain)
won(Team, TankList):-
	otherTeam(Team, Other),
	not(member(tank(Other, _), TankList)).

% possibleMoves\4 is True if Moves contains all the possible
% lists of Moves from a given Location. A series of moves is stopped
% if a win state is reached. 
% Checks for win state

possibleMoves(TankLocation, TankList, [], _):- 
	member(tank(Team, TankLocation), TankList),
	otherTeam(Team, Other),
	not(member(tank(Other, _), TankList)),
	!.
% Branch out next level of moves
possibleMoves(TankLocation, TankList, [Move|Moves], Energy):-
	adjacent(TankLocation, Adjacent),
	move(TankLocation, Adjacent, TankList, (Move, NewTankLocation, NewTankList), Energy, EnergyAfter),
	possibleMoves(NewTankLocation, NewTankList, Moves, EnergyAfter).

% No moves left
possibleMoves(TankLocation, TankList, [], Energy):- 
	(adjacent(TankLocation, Adjacent),
	move(TankLocation, Adjacent, TankList, _, Energy, _))
	*-> false; true.
	
% True if two different teams are given
otherTeam(black, red).
otherTeam(red, black).

% fixMap plays out a game of tanks where the AI plays as both the player
% and computer. It is True if the map can be won by the player AI or if
% by adding tanks for the player AI in random positions a FixedTankList
% can be found where the player AI can win.
fixMap(TankList, TankList):- playMap(TankList, Played), won(black, Played), !.
fixMap(TankList, FixedTankList):-
	addOne(black, TankList, OneRemoved),
	fixMap(OneRemoved, FixedTankList).

% True if playing a game of tanks with the AI as both players and tanks
% starting in the positions in TankList results in the tanks being in
% the positions described by TankListAfter
playMap(TankList, TankListAfter):-
	playMapRedTurn(TankList, TankListAfter).
playMapRedTurn(TankList, TankListAfter):-
	doBestMoveForTeam(red, TankList, RedMove),
	(won(red, RedMove), TankListAfter = RedMove, !; 
		playMapBlackTurn(RedMove, TankListAfter)).
playMapBlackTurn(TankList, TankListAfter):-
	doBestMoveForTeam(black, TankList, BlackMove),
	(won(black, BlackMove), TankListAfter = BlackMove, !; 
		playMapRedTurn(BlackMove, TankListAfter)).

% True if OneAdded is the same TankList with the exception that
% there is an extra tank in team Team added in a random empty position.
addOne(Team, TankList, OneAdded):-
	emptyCities(TankList, EmptyCities),
	random_member(City, EmptyCities),
	OneAdded= [tank(Team, City)|TankList].

% True if Cities contains all the cities which are no occupied by a tank
% in TankList
emptyCities(TankList, Cities):-
	findall(City, road(City, _, _), CityLeft),
	findall(City, road(_, City, _), CityRight),
	append(CityLeft, CityRight, AllCities),
	list_to_set(AllCities, CitySet),
	removeOccupiedCities(CitySet, TankList, [], Cities).

% Helper predicate which is True if Result contains all the given cities
% which are not occupied by a tank in TankList
removeOccupiedCities([], _, Result, Result).
removeOccupiedCities([City|CitySet], TankList, Cities, Result):-
	(member(tank(_, City), TankList)) -> 
		removeOccupiedCities(CitySet, TankList, Cities, Result);
		removeOccupiedCities(CitySet, TankList, [City|Cities], Result).
