java -jar target/grpc-messenger-client-1.0-SNAPSHOT.jar [ms] [thread_count] [call_count] [repeat] [messageCount]


[ms]: milliseconds of how much service should sleep; if negative it will be forwarded to he second service after multiplied with -1

[thread_count]: calling thread count

[call_count]: how many calls should be made with one http client

[repeat]: how many times to repeat the load test

[messageCount]: number of messages(id, time, content) returned during the call. 

Ex:
```
java -jar target/grpc-messenger-client-1.0-SNAPSHOT.jar 10 20 100 5 100
```