% road(city1, city2, cost).
road(a, b, 10).
road(a, c, 5).
road(b, d, 2).
road(d, c, 10).

% City positions  required for rendering only
position(a, 50, 50).
position(b, 250, 50).
position(c, 150, 150).
position(d, 250, 250).

% Initial game state
initialTankPositions(TankList):-
	TankList = [tank(red, b), tank(black, a), tank(black, d), tank(black, c)].