# Keep Room generated code and entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
# Keep WorkManager workers (instantiated by name)
-keep class * extends androidx.work.ListenableWorker { *; }
