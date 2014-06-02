% road(city1, city2, cost).
road(a, b, 10).
road(a, c, 5).
road(b, d, 2).
road(d, c, 10).

% City positions  required for rendering only
position(a, 50, 50).
position(b, 50, 100).
position(c, 100, 50).
position(d, 75, 75).

% Outlining valid tanks
:-dynamic tank/2.
tank(red, City):- (road(City, _, _); road(_, City, _)), !.
tank(black, City):- (road(City, _, _); road(_, City, _)), !.

% Initial game state
initialTankPositions(TankList):-
	TankList = [tank(red, a), tank(red, c), tank(black, b), tank(black, d)].

% Moves a tank from once city to an adjacent one, ensures:
% i) From and To are adjacent
% ii) There is a tank at From
% iii) There is no tank at To,
moveToPoint(From, To, Tanks, TanksMoved):-
	areAdjacent(From, To),
	(member(tank(Team, From), Tanks) -> true),
	not(member(tank(_, To), Tanks)),
	delete(Tanks, tank(Team, From), FromRemoved),
	TanksMoved = [tank(Team, To)|FromRemoved].

% Shoots with the tank at From at the tank at At, ensures:
% i) From and At are adjacent
% ii) There is a tank at From
% iii) There is a tank at At
% iv) The tanks at From and At are in different teams
fireOnPoint(From, At, Tanks, VictimRemoved):-
	areAdjacent(From, At),
	member(tank(ShooterTeam, From), Tanks), !,
	member(tank(VictimTeam, At), Tanks), !,
	dif(ShooterTeam, VictimTeam),
	delete(Tanks, tank(VictimTeam, At), VictimRemoved).


% Determines if there is a street between the two cities.
% It only searches for one result as there should only be
% one street between two cities
areAdjacent(City1, City2):-
	(road(City1, City2, _);	road(City2, City1, _)), !.

% Determines which cities are reachable from this one
adjacent(From, Adjacent):-
	road(From, Adjacent, _); road(Adjacent, From, _).

% Finds the cost to travel between a given pair of cities.
% It only searches for one cost as there should only be
% one street between two cities

cost(City1, City2, Cost):-
	(road(City1, City2, Cost); road(City2, City1, Cost)), !.

% Gives all the possible moves for a tank to an Adjacent city with specified Energy.
% Calculates the updated  tank list, new tank location, move taken and energy remaining.
move(TankLocation, Adjacent, TankList, Move, Energy, EnergyAfter):- %TODO ADD COST CHECK
	Energy >= 25, 
	fireOnPoint(TankLocation, Adjacent, TankList, NewTankList), 
	Move = (fireAt(TankLocation, Adjacent), TankLocation, NewTankList),
	EnergyAfter is Energy - 25;
	cost(TankLocation, Adjacent, Cost),
	Energy >= Cost,
	moveToPoint(TankLocation, Adjacent, TankList, NewTankList),
	Move = (moveTo(TankLocation, Adjacent), Adjacent, NewTankList),
	EnergyAfter is Energy - Cost.

tanksKillable(TankLocation, TankList, Move, Killable):-
	findall(Moves, possibleMoves(TankLocation, TankList, Moves, 100),Results),
	bestMove(Results, Move, Killable).

bestMove([], [], 0):- !.
bestMove(Moves, Move, Killable):- bestMove_(Moves, Move, Killable).
bestMove_(Moves, Move, Killable):-
	bestMove_(Moves, [], Move, 0, Killable).
bestMove_([], BestSoFar, BestSoFar, BestKillable, BestKillable).
bestMove_([First|Rest], BestSoFar, BestMove, BestKillable, Killable):- 
	aggregate_all(count, member(fireAt(_, _), First), FirstKillable),
	(FirstKillable > BestKillable -> 
		(NewBestMove = First, NewBestKillable = FirstKillable);
		(NewBestMove = BestSoFar, NewBestKillable = BestKillable)),
	bestMove_(Rest, NewBestMove, BestMove, NewBestKillable, Killable).

doBestMoveForTeam(Team, TankList, NewTankList):-
	findall(City, member(tank(Team, City), TankList), Positions),
	mapTanksKillable(Positions, TankList, MoveList),
	bestMove(MoveList, BestMove, _),
	doMoveList(BestMove, TankList, NewTankList).

doMoveList([], NewTankList, NewTankList):- !.
doMoveList([Move|Rest], TankList, NewTankList):-
	doMove(Move, TankList, MovedTankList),
	doMoveList(Rest, MovedTankList, NewTankList).

doMove(fireAt(_, Victim), TankList, UpdatedList):-
	delete(TankList, tank(_, Victim), UpdatedList).
doMove(moveTo(From, To), TankList, [tank(Team, To)|Removed]):-
	member(tank(Team, From), TankList), !,
	delete(TankList, tank(Team, From), Removed).

mapTanksKillable([], _, []):- !.
mapTanksKillable([TankLocation|Positions], TankList, [Move| Others]):-
	tanksKillable(TankLocation, TankList, Move, _),
	mapTanksKillable(Positions, TankList, Others).

% checks if the specified team has one
won(Team, TankList):-
	otherTeam(Team, Other),
	not(member(tank(Other, _), TankList)).

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
	
	
otherTeam(black, red).
otherTeam(red, black).