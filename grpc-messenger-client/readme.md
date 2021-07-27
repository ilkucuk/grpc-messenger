java -jar target/grpc-messenger-client-1.0-SNAPSHOT.jar [ms] [thread_count] [call_count] [repeat]


[ms]: milliseconds of how much service should sleep; if negative it will be forwarded to he second service after multiplied with -1

[thread_count]: calling thread count

[call_count]: how many calls should be made with one http client

[repeat]: how many times to repeat the load test

Ex:
```
java -jar target/grpc-messenger-client-1.0-SNAPSHOT.jar 100 400 200 5
```