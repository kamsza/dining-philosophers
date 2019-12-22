%%%-------------------------------------------------------------------
%%% @author kamsz
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 30. lis 2019 19:25
%%%-------------------------------------------------------------------
-module(ph2).
-author("kamsz").

%% API
-import(time, [apply_after/4]).
-export([diningPhilosophers/0]).


%%%%%%%%%%%%%%%%%%%%%%%%%% PHILOSOPHER %%%%%%%%%%%%%%%%%%%%%%%%%%
philosopher(Id, _, 0) ->
  clients_counter ! {client_left},
  io:format("Philosopher: ~w left~n", [Id]);



philosopher(Id, Forks, Cycle) ->
  MaxEatingTime = 100,
  MaxThinkingTime = 100,

  io:format("Philosopher: ~w is thinking~n", [Id]),
  sleep(rand:uniform(MaxThinkingTime)),

  case Id rem 2 of
    0 -> {Fork1, Fork2} = Forks;
    1 -> {Fork2, Fork1} = Forks
  end,

 % B=now(),
  checkFork({Id, self(), Fork1, 1}),
  receive
    {served}-> forks ! {get, {Fork1}}
  end,

  checkFork({Id, self(), Fork2, 1}),
  receive
    {served}-> forks ! {get, {Fork2}}
  end,
%  A=now(),
%  Time = timer:now_diff(A,B)/1000,
  io:format("Philosopher: ~w is eating      cycle: ~w ~n", [Id, Cycle]),

  sleep(rand:uniform(MaxEatingTime)),

  forks ! {release, {Fork1}},
  forks ! {release, {Fork2}},

  philosopher(Id, Forks, Cycle-1).


checkFork({Id, Client, Fork, TestNo}) ->
  forks ! {available, {Fork}, self()},
  receive
    {available, true} ->  Client ! {served};
    {available, false} -> io:format("Philosopher: ~w is waiting~n", [Id]),
      sleep(rand:uniform(round(math:pow(2, TestNo)))),
      checkFork({Id, Client, Fork, TestNo+1})
  end.




%%%%%%%%%%%%%%%%%%%%%%%%%%%%% FORK %%%%%%%%%%%%%%%%%%%%%%%%%%%%%


forks(ForkList, ReservedForksList,  Mutex) ->
  receive
    {get, {Fork}} ->
      mutex_acquire( Mutex ),
      %  io:format("GET  ~w  ~w  ~w ~n", [Fork, ForkList, ReservedForksList]),
      mutex_release( Mutex ),
      forks(ForkList -- [Fork], ReservedForksList -- [Fork], Mutex);


    {release, {Fork}} ->
      mutex_acquire( Mutex ),
      %   io:format("REL  ~w  ~w  ~w ~n", [Fork, ForkList, ReservedForksList]),
      mutex_release( Mutex ),
      forks([Fork | ForkList], ReservedForksList, Mutex);


    {available, {Fork}, Sender} ->
      mutex_acquire( Mutex ),

      %     io:format("AVA  ~w  ~w  ~w ~n", [Fork, ForkList, ReservedForksList]),

      case lists:member(Fork, ForkList) andalso not lists:member(Fork, ReservedForksList) of
        true ->  CurrReservedForksList = [Fork | ReservedForksList], Sender ! {available, true};
        false -> CurrReservedForksList = ReservedForksList, Sender ! {available, false}
      end,

      mutex_release( Mutex ),
      forks(ForkList, CurrReservedForksList, Mutex);

    {put_away} -> ok
  end.

%%%%%%%%%%%%%%%%%%%%%%%%%%% MUTEX %%%%%%%%%%%%%%%%%%%%%%%%%%%

mutex() ->
  receive
    {acquire, Pid} ->
      Pid ! {access, erlang:self()},
      receive
        {release, Pid} -> mutex()
      end
  end.

mutex_acquire( Pid ) ->
  Pid ! {acquire, erlang:self()},
  receive
    {access, Pid} -> ok
  end.

mutex_release( Pid ) -> Pid ! {release, erlang:self()}.


%%%%%%%%%%%%%%%%%%%%%%%%% MAIN FUNCTION %%%%%%%%%%%%%%%%%%%%%%%%%

diningPhilosophers() ->
  Clients = 5,
  Forks = [1, 2, 3, 4, 5],
  Life_span = 5,

  Mutex = erlang:spawn( fun() -> mutex() end ),

  register(restaurant, self()),
  register(forks, spawn(fun()-> forks(Forks, [], Mutex) end)),
  register(clients_counter, spawn(fun()-> clientCheck(Clients) end)),
  seatPhilosophers(Clients, Life_span).


clientCheck(Clients_Inside) ->
  receive
    {client_left} -> Curr_Clients_Inside = Clients_Inside-1
  end,

  if
    Curr_Clients_Inside == 0 -> forks ! {put_away},
      unregister(restaurant);
    true-> clientCheck(Curr_Clients_Inside)
  end.



%%%%%%%%%%%%%%%%%%%%%%%%%%%%% OTHER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sleep(T) ->
  receive
  after T ->
    true
  end.


seatPhilosophers(N, Life_Span) ->
  spawn(fun()-> philosopher(N, {N, 1}, Life_Span) end),
  seatPhilosophers_loop(N-1, Life_Span).


seatPhilosophers_loop(0, _) ->
  ok;

seatPhilosophers_loop(N, Life_Span) ->
  spawn(fun()-> philosopher(N, {N, N+1}, Life_Span) end),
  seatPhilosophers_loop(N-1, Life_Span).