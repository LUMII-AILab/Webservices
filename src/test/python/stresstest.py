#!/usr/bin/env python3
# -*- coding: utf-8 -*-

""" Stress test for the included webservices - evaluation of performance and limits """
__author__ = "Pēteris Paikens"

from twisted.internet import reactor, task  
from twisted.web.client import HTTPConnectionPool  
import treq  
import random  
from datetime import datetime

req_generated = 0  
req_made = 0  
req_done = 0
limit = 5

cooperator = task.Cooperator()

pool = HTTPConnectionPool(reactor)
# pool.maxPersistentPerHost = 2

def counter():  
    global req_generated
    global req_made
    global req_done

    '''This function gets called once a second and prints the progress at one 
    second intervals. 
    '''
    print("Requests: {} generated; {} made; {} done".format(
            req_generated, req_made, req_done))
    # reset the counters and reschedule ourselves
    req_generated = req_made = req_done = 0
    reactor.callLater(1, counter)

def body_received(body):  
    # print(body)
    global req_done
    req_done += 1

def request_done(response):  
    global req_made
    deferred = treq.json_content(response)
    req_made += 1
    deferred.addCallback(body_received)
    deferred.addErrback(lambda x: None)  # ignore errors
    return deferred

def request():  
    global pool
    deferred = treq.get('http://127.0.0.1:8182/domenims/%C5%A1aurslie%C5%BEudzelzsce%C4%BC%C5%A1', pool=pool)
    deferred.addCallback(request_done)
    return deferred

def requests_generator():  
    global req_generated
    global req_done
    global limit
    while True:
        if req_generated - req_done < limit: # Cap number of queued connections to the limit
            deferred = request()
            req_generated += 1
        # do not yield deferred here so cooperator won't pause until
        # response is received
        yield None

if __name__ == '__main__':  
    # make cooperator work on spawning requests
    cooperator.cooperate(requests_generator())

    # run the counter that will be reporting sending speed once a second
    reactor.callLater(1, counter)

    # run the reactor
    reactor.run()
