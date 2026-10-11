# ProGuard rules for AppZetaAdmin

# Firebase Firestore Property annotations
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep our project data classes and models for Firestore mapping
-keep class com.example.appzetaadmin.adminmenu.pedidosadmin.PedidoAdmin { *; }
-keep class com.example.appzetaadmin.adminmenu.ExtraItem { *; }

# RecyclerView ViewHolders
-keepclassmembers public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(android.view.View);
}

# Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
