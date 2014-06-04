% road(city1, city2, cost).
road(a, b, 5).
road(b, c, 5).
road(a, d, 5).
road(d, f, 5).
road(f, g, 10).
road(c, e, 10).
road(g, h, 20).
road(e, h, 20).
road(h, i, 30).
road(i, j, 20).
road(i, l, 20).
road(j, k, 10).
road(l, n, 10).
road(k, m, 5).
road(n, o, 5).
road(m, p, 5).
road(o, p, 5).

% City positions  required for rendering only
position(a, 50, 50).
position(b, 130, 50).
position(c, 210, 50).
position(d, 50, 130).
position(e, 210, 130).
position(f, 50, 210).
position(g, 130, 210).
position(h, 210, 210).
position(i, 290, 290).
position(j, 370, 290).
position(k, 450, 290).
position(l, 290, 370).
position(m, 450, 370).
position(n, 290, 450).
position(o, 370, 450).
position(p, 450, 450).

% Initial game state
initialTankPositions(TankList):-
	TankList = [tank(black, a), tank(red, p)].