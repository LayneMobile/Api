
# Keep classes/members we need for client functionality.
-keep @com.laynemobile.api.annotations.Keep class *
-keepclassmembers class * {
    @com.laynemobile.api.annotations.Keep *;
}
