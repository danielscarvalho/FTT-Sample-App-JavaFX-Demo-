/*
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 */

// 

// Referência
package demos.search;

//Referencia - https://www.oracle.com/technetwork/articles/java/expressfx-1665720.html

import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;

/**
 * Search demo application.
 */
public class SearchDemo extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("search_demo.fxml"),
                       ResourceBundle.getBundle("demos/search/search_demo"));

    primaryStage.setTitle("Search Demo");
    primaryStage.setWidth(650);
    primaryStage.setHeight(500);
    primaryStage.setScene(
      SceneBuilder.create()
        .root(root)
        .build()
    );
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
