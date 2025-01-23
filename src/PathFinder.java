
// PROG2 VT2024, Inl ̈amningsuppgift, del 2 // Grupp 144
// Erik Mörsell
// Marcus Lundell 
//  Ísak Jakob Hafthórsson
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class PathFinder extends Application {
    private Stage primaryStage;
    private ListGraph<PlaceNode> graph;
    private List<PlaceNode> selectedPlaces;
    private ImageView mapView;
    private boolean unsavedChanges;
    private Pane outputArea;
    private List<PlaceNode> placeMarkers;

    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 650, 800);

    // Menu Bar
    MenuBar menu = new MenuBar();
    Menu menuFile = new Menu("File");

    MenuItem menuNewMap = new MenuItem("New Map");
    MenuItem menuOpenFile = new MenuItem("Open");
    MenuItem menuSaveFile = new MenuItem("Save");
    MenuItem menuSaveImage = new MenuItem("Save Image");
    MenuItem menuExit = new MenuItem("Exit");

    ToolBar toolBar = new ToolBar();
    Button btnFindPath = new Button("Find Path");
    Button btnShowConnection = new Button("Show Connection");
    Button btnNewPlace = new Button("New Place");
    Button btnNewConnection = new Button("New Connection");
    Button btnChangeConnection = new Button("Change Connection");

    HBox buttonBox = new HBox(5);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("PathFinder");

        graph = new ListGraph<>();
        selectedPlaces = new ArrayList<>();
        placeMarkers = new ArrayList<>();
        unsavedChanges = false;

        menuFile.getItems().addAll(menuNewMap, menuOpenFile, menuSaveFile, menuSaveImage, menuExit);
        menu.getMenus().add(menuFile);
        menu.setId("menu");
        menuFile.setId("menuFile");
        menuNewMap.setId("menuNewMap");
        menuOpenFile.setId("menuOpenFile");
        menuSaveFile.setId("menuSaveFile");
        menuSaveImage.setId("menuSaveImage");
        menuExit.setId("menuExit");
        btnFindPath.setId("btnFindPath");
        btnShowConnection.setId("btnShowConnection");
        btnNewPlace.setId("btnNewPlace");
        btnNewConnection.setId("btnNewConnection");
        btnChangeConnection.setId("btnChangeConnection");

        buttonBox.getChildren().addAll(btnFindPath, btnShowConnection, btnNewPlace, btnNewConnection, btnChangeConnection);
        toolBar.getItems().add(buttonBox);

        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menu, toolBar);
        root.setTop(topContainer);

        mapView = new ImageView();
        outputArea = new Pane();
        outputArea.getChildren().addAll(mapView);
        root.setCenter(outputArea);
        outputArea.setId("outputArea");

        menuNewMap.setOnAction(e -> handleUnsavedChanges(primaryStage, this::loadNewMap));
        menuOpenFile.setOnAction(e -> handleUnsavedChanges(primaryStage, this::openGraph));
        menuSaveFile.setOnAction(e -> saveGraph());
        menuSaveImage.setOnAction(e -> saveImage(primaryStage));
        menuExit.setOnAction(e -> handleUnsavedChanges(primaryStage, this::exitApplication));

        btnFindPath.setOnAction(e -> showPath());
        btnShowConnection.setOnAction(e -> showConnection());
        btnNewPlace.setOnAction(e -> createNewPlace());
        btnNewConnection.setOnAction(e -> createNewConnection());
        btnChangeConnection.setOnAction(e -> changeConnection());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleUnsavedChanges(Stage primaryStage, Runnable action) {
        if (unsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning!");
            alert.setTitle("Unsaved Changes");
            alert.setContentText("There are Unsaved changes, exit anyway?");


            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                outputArea.getChildren().clear();
                mapView = new ImageView();
                outputArea.getChildren().add(mapView);
                graph = new ListGraph<>();
                placeMarkers.clear();
                selectedPlaces.clear();
                unsavedChanges = false;

                outputArea.setOnMouseClicked(null);

                action.run();
            }
        } else {
            action.run();
        }
    }



    private void loadNewMap() {
        outputArea.getChildren().clear();
        mapView = new ImageView();
        outputArea.getChildren().add(mapView);
        graph = new ListGraph<>();
        placeMarkers.clear();
        selectedPlaces.clear();
        unsavedChanges = false;

        outputArea.setOnMouseClicked(null);
        unsavedChanges = true;
        try {

            Image image = new Image("file:europa2.gif");
            mapView.setImage(image);

        } catch (Exception e) {
            showAlert("Error", "Unable to load map image: " + e.getMessage());
        }
        unsavedChanges = true;
    }

    private void createNewPlace() {
        outputArea.setCursor(javafx.scene.Cursor.CROSSHAIR);
        btnNewPlace.setDisable(true);
        outputArea.setOnMouseClicked(this::handleMapClickNewPlace);
        unsavedChanges = true;
    }

    private void handleMapClickNewPlace(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Place");
            dialog.setHeaderText("Enter the name of the new place:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    PlaceNode newPlaceNode = new PlaceNode(name, event.getX(), event.getY(), 5);
                    graph.add(newPlaceNode);

                    addPlaceToMap(newPlaceNode);
                }
            });

            outputArea.setCursor(javafx.scene.Cursor.DEFAULT);
            btnNewPlace.setDisable(false);
            outputArea.setOnMouseClicked(null);
        }
    }

    private void addPlaceToMap(PlaceNode placeNode) {
        placeNode.setOnMouseClicked(event -> handlePlaceClick(placeNode));
        placeMarkers.add(placeNode);

        Label placeLabel = new Label(placeNode.getPlaceName());
        placeLabel.setLayoutX(placeNode.getCenterX() + 5);
        placeLabel.setLayoutY(placeNode.getCenterY() - 10);

        outputArea.getChildren().addAll(placeNode, placeLabel);
        unsavedChanges = true;
    }

    private void handlePlaceClick(PlaceNode placeNode) {
        if (selectedPlaces.contains(placeNode)) {
            placeNode.setFill(Color.BLUE);
            selectedPlaces.remove(placeNode);
        } else {
            if (selectedPlaces.size() < 2) {
                placeNode.setFill(Color.RED);
                selectedPlaces.add(placeNode);
            }
        }
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showPath() {
        if (selectedPlaces.size() != 2) {
            showAlert("Error", "Please select exactly two places to find a path.");
            return;
        }

        PlaceNode fromPlace = selectedPlaces.get(0);
        PlaceNode toPlace = selectedPlaces.get(1);

        List<Edge<PlaceNode>> path = graph.getPath(fromPlace, toPlace);

        if (path == null || path.isEmpty()) {
            showAlert("No Path", "No path found between " + fromPlace.getPlaceName() + " and " + toPlace.getPlaceName() + ".");
        } else {
            StringBuilder message = new StringBuilder();
            int totalTime = 0;

            for (Edge<PlaceNode> edge : path) {
                PlaceNode nextPlace = edge.getDestination();
                message.append("to ").append(nextPlace.getPlaceName()).append(" by ")
                        .append(edge.getName()).append(" takes ").append(edge.getWeight()).append("\n");

                totalTime += edge.getWeight();
            }

            message.append("Total ").append(totalTime).append("\n");

            String header = "The Path from " + fromPlace.getPlaceName() + " to " + toPlace.getPlaceName() + ":";


            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Path Information");
            dialog.setHeaderText(header);

            TextArea textArea = new TextArea(message.toString());
            textArea.setEditable(false);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Path Details:"), 0, 0);
            grid.add(textArea, 0, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            dialog.showAndWait();
        }
    }

    private void showConnection() {
        if (selectedPlaces.size() != 2) {
            showAlert("Error", "Please select exactly two places to show connection information.");
            return;
        }

        PlaceNode fromPlace = selectedPlaces.get(0);
        PlaceNode toPlace = selectedPlaces.get(1);

        Edge<PlaceNode> connection = graph.getEdgeBetween(fromPlace, toPlace);
        if (connection == null) {
            showAlert("Error", "No connection exists between the selected places.");
            return;
        }


        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connection");
        dialog.setHeaderText("Connection from " + fromPlace.getPlaceName() + " to " + toPlace.getPlaceName());


        TextField nameField = new TextField(connection.getName());
        nameField.setDisable(true); // Disable editing
        Label nameLabel = new Label("Name:");
        nameLabel.setLabelFor(nameField);

        TextField weightField = new TextField(String.valueOf(connection.getWeight()));
        weightField.setDisable(true); // Disable editing
        Label weightLabel = new Label("Time:");
        weightLabel.setLabelFor(weightField);


        GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.addRow(0, nameLabel, nameField);
        content.addRow(1, weightLabel, weightField);
        dialog.getDialogPane().setContent(content);


        dialog.showAndWait();
    }


    private void createNewConnection() {
        if (selectedPlaces.size() != 2) {
            showAlert("Error", "Please select exactly two places to create a connection.");
            return;
        }

        PlaceNode fromPlace = selectedPlaces.get(0);
        PlaceNode toPlace = selectedPlaces.get(1);

        if (graph.getEdgeBetween(fromPlace, toPlace) != null) {
            showAlert("Error", "A connection already exists between the selected places.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connection");
        dialog.setHeaderText("Connection from " + fromPlace.getPlaceName() + " to " + toPlace.getPlaceName());

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        Label nameLabel = new Label("Name:");
        nameLabel.setLabelFor(nameField);

        TextField weightField = new TextField();
        weightField.setPromptText("Time");
        Label weightLabel = new Label("Time:");
        weightLabel.setLabelFor(weightField);

        GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.addRow(0, nameLabel, nameField);
        content.addRow(1, weightLabel, weightField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText();
                String weight = weightField.getText();
                if (!name.isEmpty() && !weight.isEmpty()) {
                    return name + ";" + weight;
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String[] parts = pair.split(";");
            if (parts.length == 2) {
                String name = parts[0];
                int weight = Integer.parseInt(parts[1]);
                graph.connect(fromPlace, toPlace, name, weight);
                displayConnectionOnMap(fromPlace, toPlace);

            } else {
                showAlert("Error", "Invalid input format. Please use the format: name;weight");
            }
        });
        unsavedChanges = true;
    }

    private void displayConnectionOnMap(PlaceNode fromPlace, PlaceNode toPlace) {
        Line connectionLine = new Line(fromPlace.getCenterX(), fromPlace.getCenterY(), toPlace.getCenterX(), toPlace.getCenterY());
        connectionLine.setStroke(Color.BLACK);
        connectionLine.setDisable(true);

        outputArea.getChildren().add(connectionLine);
    }

    private void changeConnection() {
        if (selectedPlaces.size() != 2) {
            showAlert("Error", "Please select exactly two places to change a connection.");
            return;
        }

        PlaceNode fromPlace = selectedPlaces.get(0);
        PlaceNode toPlace = selectedPlaces.get(1);

        Edge<PlaceNode> connection = graph.getEdgeBetween(fromPlace, toPlace);
        if (connection == null) {
            showAlert("Error", "No connection exists between the selected places.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Connection");
        dialog.setHeaderText("Change the connection between " + fromPlace.getPlaceName() + " and " + toPlace.getPlaceName());

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setText(connection.getName());
        nameField.setDisable(true); // Make the name field read-only

        TextField weightField = new TextField();
        weightField.setPromptText("Time");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(weightField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText();
                String weightText = weightField.getText();
                if (!name.isEmpty() && !weightText.isEmpty()) {
                    try {
                        int weight = Integer.parseInt(weightText);
                        return name + ";" + weight;
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Invalid time input. Please enter a valid integer.");
                    }
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String[] parts = pair.split(";");
            if (parts.length == 2) {
                String newName = parts[0];
                int newWeight = Integer.parseInt(parts[1]);


                PlaceNode fromNode = getPlaceNodeByName(fromPlace.getPlaceName());
                PlaceNode toNode = getPlaceNodeByName(toPlace.getPlaceName());

                if (fromNode != null && toNode != null) {
                    graph.setConnectionWeight(fromNode, toNode, newWeight);
                } else {
                    showAlert("Error", "One or both places not found.");
                }
            } else {
                showAlert("Error", "Invalid input format. Please provide both name and time.");
            }
        });
        unsavedChanges = true;
    }


    private PlaceNode getPlaceNodeByName(String nodeName) {
        for (PlaceNode node : graph.getNodes()) {
            if (node.getPlaceName().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }

    private void openGraph() {
        outputArea.getChildren().clear();
        mapView = new ImageView();
        outputArea.getChildren().add(mapView);
        graph = new ListGraph<>();
        placeMarkers.clear();
        selectedPlaces.clear();
        unsavedChanges = false;

        outputArea.setOnMouseClicked(null);

        File file = new File("europa.graph");
        if (!file.exists()) {
            showAlert("Error", "File not found: europa.graph");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            outputArea.getChildren().clear();
            placeMarkers.clear();
            graph = new ListGraph<>();

            String mapUrl = reader.readLine();
            if (mapUrl != null && mapUrl.startsWith("file")) {
                Image mapImage = new Image(mapUrl);
                mapView.setImage(mapImage);

                outputArea.getChildren().add(mapView);
            } else {
                showAlert("Error", "Invalid map image URL format.");
                return;
            }

            String nodeLine = reader.readLine();
            if (nodeLine != null) {
                String[] nodeData = nodeLine.split(";");
                for (int i = 0; i < nodeData.length; i += 3) {
                    String nodeName = nodeData[i];
                    double x = Double.parseDouble(nodeData[i + 1]);
                    double y = Double.parseDouble(nodeData[i + 2]);
                    PlaceNode placeNode = new PlaceNode(nodeName, x, y, 5);
                    graph.add(placeNode);
                    addPlaceToMap(placeNode);
                }
            }

            String connectionLine;
            while ((connectionLine = reader.readLine()) != null) {
                String[] connectionData = connectionLine.split(";");
                String fromNodeName = connectionData[0];
                String toNodeName = connectionData[1];
                String connectionName = connectionData[2];
                int weight = Integer.parseInt(connectionData[3]);

                PlaceNode fromNode = getPlaceNodeByName(fromNodeName);
                PlaceNode toNode = getPlaceNodeByName(toNodeName);

                if (fromNode != null && toNode != null) {
                    boolean edgeExists = false;
                    for (Edge<PlaceNode> edge : graph.getEdgesFrom(fromNode)) {
                        if (edge.getDestination().equals(toNode)) {
                            edgeExists = true;
                            break;
                        }
                    }

                    if (!edgeExists) {
                        graph.connect(fromNode, toNode, connectionName, weight);
                        displayConnectionOnMap(fromNode, toNode);
                    }
                } else {
                    showAlert("Error", "Invalid connection nodes found in file.");
                }
            }

        } catch (IOException e) {
            showAlert("Error", "Failed to read the file: " + e.getMessage());
        }
        unsavedChanges = false;

    }


    private void saveGraph() {
        String fileName = "europa.graph";
        File file = new File(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String mapUrl = mapView.getImage().getUrl();
            writer.write(mapUrl);
            writer.newLine();

            StringBuilder nodeLine = new StringBuilder();
            for (PlaceNode placeNode : placeMarkers) {
                String nodeName = placeNode.getPlaceName();
                double x = placeNode.getCenterX();
                double y = placeNode.getCenterY();
                nodeLine.append(nodeName).append(";").append(x).append(";").append(y).append(";");
            }


            if (nodeLine.length() > 0) {
                nodeLine.deleteCharAt(nodeLine.length() - 1);
            }

            writer.write(nodeLine.toString());
            writer.newLine();

            Set<String> writtenEdges = new HashSet<>();
            for (PlaceNode node : graph.getNodes()) {
                for (Edge<PlaceNode> edge : graph.getEdgesFrom(node)) {
                    String edgeKey = node.getPlaceName() + "-" + edge.getDestination().getPlaceName();
                    String reverseEdgeKey = edge.getDestination().getPlaceName() + "-" + node.getPlaceName();
                    if (!writtenEdges.contains(edgeKey) && !writtenEdges.contains(reverseEdgeKey)) {
                        String fromNode = node.getPlaceName();
                        String toNode = edge.getDestination().getPlaceName();
                        String connectionName = edge.getName();
                        int weight = edge.getWeight();
                        writer.write(fromNode + ";" + toNode + ";" + connectionName + ";" + weight);
                        writer.newLine();
                        writer.write(toNode + ";" + fromNode + ";" + connectionName + ";" + weight);
                        writer.newLine();
                        writtenEdges.add(edgeKey);
                        writtenEdges.add(reverseEdgeKey);
                    }
                }
            }

            for (PlaceNode node : graph.getNodes()) {
                for (Edge<PlaceNode> edge : graph.getEdgesFrom(node)) {
                    graph.disconnect(node, edge.getDestination());
                }
            }

            unsavedChanges = false;

        } catch (IOException e) {
            showAlert("Error", "Failed to save the file: " + e.getMessage());
        }
    }

    private void saveImage(Stage stage) {

        WritableImage image = root.snapshot(new SnapshotParameters(), null);
        File file = new File("capture.png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

        } catch (IOException e) {

        }
    }

    private void exitApplication() {
        handleUnsavedChanges(primaryStage, () -> {
            if (primaryStage != null) {
                WindowEvent windowCloseEvent = new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST);
                primaryStage.fireEvent(windowCloseEvent);
            } else {
                throw new IllegalStateException("Primary stage is not initialized.");
            }
        });
    }
}