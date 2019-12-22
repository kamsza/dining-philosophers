%%%-------------------------------------------------------------------
%%% @author kamsz
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 01. gru 2019 14:28
%%%-------------------------------------------------------------------
-module(ph3).
-author("kamsz").

%% API
%% PHILOSOPHERS WITH WAITER
-export([diningPhilosophers/0]).


%%%%%%%%%%%%%%%%%%%%%%%%%% PHILOSOPHER %%%%%%%%%%%%%%%%%%%%%%%%%%
philosopher(Id, _, 0) ->
  waiter ! {unregister},
  io:format("Philosopher: ~w left~n", [Id]);



philosopher(Id, Forks, Cycle) ->
  MaxEatingTime = 5,
  MaxThinkingTime = 5,

  io:format("Philosopher: ~w is thinking~n", [Id]),
  sleep(rand:uniform(MaxThinkingTime)),
 % B=now(),
  waiter ! {waiting, {self(), Forks}},

  receive
    {served}-> forks ! {get, Forks},
      waiter ! {eating},
      io:format("Philosopher: ~w is eating      cycle: ~w ~n", [Id, Cycle])
  end,
%  A=now(),
 % Time = timer:now_diff(A,B)/1000,

  sleep(rand:uniform(MaxEatingTime)),

  forks ! {release, Forks},
  waiter ! {finished},

  philosopher(Id, Forks, Cycle-1).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%% FORK %%%%%%%%%%%%%%%%%%%%%%%%%%%%%

forks(ForkList, ReservedForksList,  Mutex) ->
  receive
    {get, {Left, Right}} ->
      mutex_acquire( Mutex ),
      mutex_release( Mutex ),
      forks(ForkList -- [Left, Right], ReservedForksList -- [Left, Right], Mutex);


    {release, {Left, Right}} ->
      mutex_acquire( Mutex ),
      mutex_release( Mutex ),
      forks([Left, Right | ForkList], ReservedForksList, Mutex);


    {available, {Left, Right}, Sender} ->
      mutex_acquire( Mutex ),

      case lists:member(Left, ForkList) andalso lists:member(Right, ForkList)
        andalso  not lists:member(Left, ReservedForksList) andalso not lists:member(Right, ReservedForksList) of
        true ->  CurrReservedForksList = [Left, Right | ReservedForksList], Sender ! {available, true};
        false -> CurrReservedForksList = ReservedForksList, Sender ! {available, false}
      end,

      mutex_release( Mutex ),
      forks(ForkList, CurrReservedForksList, Mutex);

    {put_away} -> ok
  end.

checkForks(Client, Forks, TestNo) ->
  forks ! {available, Forks, self()},
  receive
    {available, true} ->  Client ! {served};
    {available, false} ->
      sleep(rand:uniform(round(math:pow(2, TestNo)))),
      checkForks(Client, Forks, TestNo+1)
  end.

%%%%%%%%%%%%%%%%%%%%%%%%%%%% WAITER %%%%%%%%%%%%%%%%%%%%%%%%%%%%

waiter(0, 0) ->
  restaurant ! {waiter_left},
  io:format("Waiter ENDED.~n");


waiter(ClientCount, EatingCount) ->
  receive
    {waiting, {Client, Forks}} ->
      case (EatingCount < 2) of
        true ->	checkForks(Client, Forks, 1);
        false -> sleep(10), self() ! {waiting, {Client, Forks}}
      end,
      waiter(ClientCount, EatingCount);

    {eating} ->
      waiter(ClientCount, EatingCount+1);

    {finished} ->
      waiter(ClientCount, EatingCount-1);
    {unregister} ->
      waiter(ClientCount-1, EatingCount)
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
  Forks = [1, 2, 3, 4, 5],
  Clients = 5,
  Life_span = 10,

  Mutex = erlang:spawn( fun() -> mutex() end ),

  register(restaurant, self()),
  register(forks, spawn(fun()-> forks(Forks, [] ,Mutex) end)),
  register(waiter, spawn(fun()-> waiter(Clients, 0) end)),

  seatPhilosophers(Clients, Life_span),

  receive
    {waiter_left} -> forks ! {put_away},
      unregister(restaurant)
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