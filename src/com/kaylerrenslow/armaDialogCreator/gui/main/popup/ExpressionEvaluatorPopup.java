package com.kaylerrenslow.armaDialogCreator.gui.main.popup;

import com.kaylerrenslow.armaDialogCreator.expression.*;
import com.kaylerrenslow.armaDialogCreator.gui.fxcontrol.SyntaxTextArea;
import com.kaylerrenslow.armaDialogCreator.gui.popup.StagePopup;
import com.kaylerrenslow.armaDialogCreator.main.ArmaDialogCreator;
import com.kaylerrenslow.armaDialogCreator.main.Lang;
import com.kaylerrenslow.armaDialogCreator.util.KeyValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Popup that has a place for the user to test expressions and see outcomes in the return value and environment overview.

 @author Kayler
 @since 05/24/2017 */
public class ExpressionEvaluatorPopup extends StagePopup<VBox> {

	@NotNull
	private static String getValueAsString(@NotNull Value v) {
		if (v == Value.Void) {
			return "nil";
		}
		if (v instanceof Value.StringLiteral) {
			return ((Value.StringLiteral) v).getAsDisplayableArmaString();
		}
		return v.toString();
	}

	private final ResourceBundle bundle = Lang.getBundle("ExpressionEvaluatorPopupBundle");
	private final EnvOverviewPane environmentOverviewPane = new EnvOverviewPane();
	private final CodeAreaPane codeAreaPane = new CodeAreaPane();
	private final StackPane stackPaneResult = new StackPane();
	private final TextArea taConsole = new TextArea();
	private final StackPane stackPaneConsole = new StackPane();
	private boolean showingConsole = false;

	public ExpressionEvaluatorPopup() {
		super(ArmaDialogCreator.getPrimaryStage(), new VBox(0), null);

		setTitle(bundle.getString("popup_title"));
		setStageSize(820, 550);

		taConsole.setText(bundle.getString("CodeArea.console_init") + " ");
		String[] commands = ExpressionInterpreter.getSupportedCommands();
		for (String command : commands) {
			taConsole.appendText(command != commands[commands.length - 1] ? command + ", " : command);
		}
		taConsole.appendText("\n\n");

		//setup toolbar
		Button btnEval = new Button(bundle.getString("Toolbar.evaluate"));
		btnEval.setOnAction(event -> evaluateText());
		Button btnToggleConsole = new Button(bundle.getString("Toolbar.toggle_console"));
		btnToggleConsole.setOnAction(event -> toggleConsole());
		ToolBar toolBar = new ToolBar(btnEval, btnToggleConsole);

		myRootElement.getChildren().add(toolBar);

		VBox vboxAfterToolBar = new VBox(10);
		VBox.setVgrow(vboxAfterToolBar, Priority.ALWAYS);
		vboxAfterToolBar.setPadding(new Insets(10));
		vboxAfterToolBar.setMinWidth(300);
		VBox vboxEnvOverview = new VBox(5, new Label(bundle.getString("EnvOverview.label")), environmentOverviewPane);
		HBox hbox = new HBox(5, codeAreaPane, vboxEnvOverview);
		HBox.setHgrow(codeAreaPane, Priority.ALWAYS);
		HBox.setHgrow(vboxEnvOverview, Priority.SOMETIMES);

		VBox.setVgrow(hbox, Priority.ALWAYS);
		vboxAfterToolBar.getChildren().add(hbox);
		vboxAfterToolBar.getChildren().add(stackPaneConsole);

		vboxAfterToolBar.getChildren().add(new HBox(5, new Label(bundle.getString("CodeArea.return_value")), stackPaneResult));

		ScrollPane scrollPane = new ScrollPane(vboxAfterToolBar);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);

		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		myRootElement.getChildren().add(scrollPane);

	}

	private void toggleConsole() {
		if (showingConsole) {
			stackPaneConsole.getChildren().clear();
		} else {
			stackPaneConsole.getChildren().add(taConsole);
		}
		showingConsole = !showingConsole;

	}

	private void evaluateText() {
		SimpleEnv env = new SimpleEnv();
		stackPaneResult.getChildren().clear();
		String returnValueString;
		String consoleString;
		try {
			Value returnValue = ExpressionInterpreter.getInstance().evaluateStatements(codeAreaPane.getText(), env);
			returnValueString = getValueAsString(returnValue);
			consoleString = bundle.getString("CodeArea.success");
		} catch (ExpressionEvaluationException e) {
			returnValueString = bundle.getString("CodeArea.error");
			consoleString = e.getMessage();
		}
		stackPaneResult.getChildren().add(new Label(returnValueString));
		environmentOverviewPane.setEnv(env);
		taConsole.appendText(consoleString + "\n");
	}

	private class CodeAreaPane extends SyntaxTextArea {
		@RegExp
		private final String decimal = "\\.[0-9]+|[0-9]+\\.[0-9]+";
		@RegExp
		private final String integer = "[0-9]+";
		private final String exponent = String.format("(%s|%s)[Ee][+-]?[0-9]*", integer, decimal);
		@RegExp
		private final String hex = "0[xX]0*[0-9a-fA-F]+";

		private final Pattern pattern = Pattern.compile(
				"(?<IDENTIFIER>[a-zA-Z_$][a-zA-Z_$0-9]+)" +
						String.format("|(?<NUMBER>%s|%s|%s|%s)", integer, decimal, exponent, hex) +
						"|(?<STRING>('[^']*')+|(\"[^\"]*\")+)"
		);

		private final String[] supportedCommands = ExpressionInterpreter.getSupportedCommands();

		public CodeAreaPane() {
			getStylesheets().add("/com/kaylerrenslow/armaDialogCreator/gui/expressionSyntax.css");
			richChanges()
					.filter(c -> !c.getInserted().equals(c.getRemoved()))
					.subscribe(c -> {
						setStyleSpans(0, computeHighlighting(getText()));
					});
			getStyleClass().add("bordered-syntax-text-area");
		}

		private StyleSpans<Collection<String>> computeHighlighting(String text) {
			Matcher matcher = pattern.matcher(text);
			int lastKwEnd = 0;
			StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

			while (matcher.find()) {
				String styleClass = null;
				String s;
				if ((s = matcher.group("IDENTIFIER")) != null) {
					for (String command : supportedCommands) {
						if (s.equals(command)) {
							styleClass = "command";
							break;
						}
					}
				} else if (matcher.group("NUMBER") != null) {
					styleClass = "number";
				} else if (matcher.group("STRING") != null) {
					styleClass = "string";
				}

				assert styleClass != null; //this should never happen

				spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
				spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
				lastKwEnd = matcher.end();
			}
			spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
			return spansBuilder.create();
		}
	}

	private class EnvOverviewPane extends StackPane {

		private final ListView<String> listView = new ListView<>();

		public EnvOverviewPane() {
			getChildren().add(listView);
			listView.setPlaceholder(new Label(bundle.getString("EnvOverview.no_env")));
			listView.setMinWidth(300);
			listView.setStyle("-fx-font-family:monospace");
			VBox.setVgrow(this, Priority.ALWAYS);
		}

		public void setEnv(@NotNull Env e) {
			listView.getItems().clear();
			List<KeyValue<String, Value>> list = new ArrayList<>();
			for (KeyValue<String, Value> kv : e) {
				list.add(kv);
			}
			int maxVarLength = 1;
			for (KeyValue<String, Value> kv : list) {
				maxVarLength = Math.max(kv.getKey().length(), maxVarLength);
			}
			for (KeyValue<String, Value> kv : list) {
				listView.getItems().add(String.format("%-" + maxVarLength + "s = %s", kv.getKey(), getValueAsString(kv.getValue())));
			}
		}
	}

}
