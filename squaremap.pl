% road(city1, city2, cost).
road(one, two, 30).
road(one, four, 30).
road(one, six, 30).
road(two, three, 30).
road(two, four, 30).
road(two, five, 30).
road(two, seven, 30).
road(three, five, 30).
road(three, eight, 30).
road(four, six, 30).
road(four, seven, 30).
road(five, seven, 30).
road(five, eight, 30).
road(six, seven, 30).
road(six, nine, 30).
road(six, eleven, 30).
road(seven, eight, 30).
road(seven, nine, 30).
road(seven, ten, 30).
road(seven, twelve, 30).
road(eight, ten, 30).
road(eight, thirteen, 30).
road(nine, eleven, 30).
road(nine, twelve, 30).
road(ten, twelve, 30).
road(ten, thirteen, 30).
road(eleven, twelve, 30).
road(twelve, thirteen, 30).

% City positions  required for rendering only
position(one, 50, 50).
position(two, 250, 50).
position(three, 450, 50).
position(four, 150, 150).
position(five, 350, 150).
position(six, 50, 250).
position(seven, 250, 250).
position(eight, 450, 250).
position(nine, 150, 350).
position(ten, 350, 350).
position(eleven, 50, 450).
position(twelve, 250, 450).
position(thirteen, 450, 450).


% Initial game state
initialTankPositions(TankList):-
	TankList = [tank(black, six), tank(black, eleven), tank(black, twelve), tank(red, three), tank(red, two), tank(red, eight)].