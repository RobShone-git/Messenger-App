module com.example.chatproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.voip to javafx.fxml;
    exports com.example.voip;
}