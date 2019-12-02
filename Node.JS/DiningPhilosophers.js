var async = require("async");

var Fork = function() {
    this.state = 0;
    return this;
}

Fork.prototype.acquire = function(cb) {
       var binaryBackoff = function(fork, time) {
        if(fork.state === 1) {
            setTimeout(function() { binaryBackoff(fork, time * 2); }, time);
        } else {
            fork.state = 1;
            cb();
        }
    }

    setTimeout(binaryBackoff, 1, this, 1)
}

Fork.prototype.release = function() {
    this.state = 0;
}


var Philosopher = function(id, forks) {
    this.id = id;
    this.forks = forks;
    this.f1 = id % forks.length;
    this.f2 = (id+1) % forks.length;
    return this;
}

Philosopher.prototype.startNaive = function(count) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    var eat = function(cb) {
        var t0 = Date.now();
        forks[f1].acquire(function() {
            forks[f2].acquire(function() {
                console.log("Philosopher " + id + " is eating, waiting time: " + (Date.now()-t0));
                setTimeout(function () {
                    forks[f1].release();
                    forks[f2].release();

                    cb();
                }, 100 * Math.random())
            })
        })
    }

    var tasks = []
    for(var i = 0; i < count; i++)
        tasks.push(eat)

    async.waterfall(tasks);
}

Philosopher.prototype.startAsym = function(count) {
    var forks = this.forks,
        f1,
        f2,
        id = this.id;

        if (this.f1 % 2 == 0) {
            f1 = this.f1;
            f2 = this.f2;
        } else {
            f2 = this.f1;
            f1 = this.f2;
        }

    var eat = function(cb) {
        var t0 = Date.now();
        forks[f1].acquire(function() {
            forks[f2].acquire(function() {
                console.log("Philosopher " + id + " is eating, waiting time: " + (Date.now()-t0));
                setTimeout(function () {
                    forks[f1].release();
                    forks[f2].release();

                    cb();
                }, 100 * Math.random())
            })
        })
    }

    var tasks = []
    for(var i = 0; i < count; i++)
        tasks.push(eat)

    async.waterfall(tasks);
}

var Waiter = function(forks) {
    this.busy = false;
    this.forks = forks;
    return this;
}

Waiter.prototype.acquire = function(cb) {
    var binaryBackoff = function(cb, conductor, time) {
        if(conductor.busy) {
            setTimeout(function() { return binaryBackoff(cb, conductor, time * 2); }, time);
        } else {
            conductor.busy = true;
            cb();
        }
    }
    binaryBackoff(cb, this, 1);
}

Waiter.prototype.release = function() {
    this.busy = false;
}

Philosopher.prototype.startConductor = function(count, waiter) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id,
        waiter = waiter;


    var eat = function(cb) {
        var t0 = Date.now();
        waiter.acquire(function() {
            forks[f1].acquire(function() {
                forks[f2].acquire(function() {
                    console.log("Philosopher " + id + " is eating, waiting time: " + (Date.now()-t0));
                    setTimeout(function () {
                        waiter.release();
                        forks[f1].release();
                        forks[f2].release();

                        cb();
                    }, 100 * Math.random())
                })
            })
        })
    }

    var tasks = []
    for(var i = 0; i < count; i++)
        tasks.push(eat)

    async.waterfall(tasks);
}


var N = 5;
var forks = [];
var philosophers = [];
var waiter = new Waiter();
var meals = 10;

for (var i = 0; i < N; i++) {
    forks.push(new Fork());
}

for (var i = 0; i < N; i++) {
    philosophers.push(new Philosopher(i, forks));
}

for (var i = 0; i < N; i++) {
  //   philosophers[i].startNaive(meals);
     philosophers[i].startAsym(meals);
 //   philosophers[i].startConductor(meals, waiter);
}

