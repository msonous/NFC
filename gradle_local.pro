# Common DexGuard configuration for debug versions and release versions.
# Copyright (c) 2012-2014 Saikoa / Itsana BVBA


#-libraryjars E:\MIFARE_INNOVATIONS_BUILD\mifaresdk_code\trunk\05_BuildnTools\0501_BuildScripts\jsr268library.jar
-libraryjars C:\Users\nxp69448\My Softwares\android_adt\sdk\platforms\android-22\android.jar


# Keep some attributes that the compiler needs.
-keepattributes Exceptions,Deprecated,EnclosingMethod

# Keep all public API.
-keep public class * {
    public protected *;
}


