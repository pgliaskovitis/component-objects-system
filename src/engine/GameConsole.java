/*
* Copyright 2012 Periklis G. Liaskovitis
*
* This file is part of component-objects-system.
*
* component-objects-system is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* component-objects-system is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with component-objects-system. If not, see <http://www.gnu.org/licenses/>.
*/

package engine;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;

public class GameConsole extends WindowAdapter implements WindowListener, Runnable {

	private JFrame frame;

	private JTextArea textArea;

	private static final Font userFont = new Font("Book Antiqua", Font.PLAIN, 13);
	private static final Font gameFont = new Font("Monotype Corsiva", Font.ITALIC, 13);

	private Thread readerUser;
	private Thread readerGame;

	private volatile boolean quit = false;
	private volatile boolean waitingUserInput = false;

	private int currentLineNum = 0;

	private TextAreaStreamer inUser; //console reads user input from here
	InputStreamReader inUserRawReader;
	BufferedReader inUserBufferedReader;

	private PipedOutputStream outGame; //game writes here
	private PrintStream formattedOutGame;

	private final PipedInputStream inGame = new PipedInputStream(); //console reads game input from here
	InputStreamReader inGameRawReader = new InputStreamReader(inGame);
	BufferedReader inGameBufferedReader = new BufferedReader(inGameRawReader);

	private final BlockingQueue<String> userInputBuffer = new LinkedBlockingQueue<String>();

	public GameConsole() {

		// create all components and add them
		frame = new JFrame("The Phoenix of Level 9 Computing");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = new Dimension((int) (screenSize.width / 2), (int) (screenSize.height / 2));
		int x = (int) (frameSize.width / 2);
		int y = (int) (frameSize.height / 2);
		frame.setBounds(x, y, frameSize.width, frameSize.height);

		textArea = new JTextArea();
		textArea.setEditable(true);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
		frame.setVisible(true);

		frame.addWindowListener(this);

		//normal state is to have user input redirected to the output stream at all times
		try {

			inUser = new TextAreaStreamer(textArea);
			inUserRawReader = new InputStreamReader(inUser);
			inUserBufferedReader = new BufferedReader(inUserRawReader);

			// read() is only supposed to be called when someone actually calls System.in.read() or similar
			textArea.setFont(userFont);
			textArea.addKeyListener(inUser);
			System.setIn(inUser);

			//also initialize game out stream
			outGame = new PipedOutputStream(inGame);
			formattedOutGame = new PrintStream(outGame, true);
		} catch (IOException io) {

		} catch (SecurityException se) {

		}

		quit = false; // signals the Threads that they should exit

		// Starting a separate thread to read from standard in
		//
		readerUser = new Thread(this);
		readerUser.setDaemon(true);
		readerUser.start();

		// Starting a separate thread to read from standard in
		//
		readerGame = new Thread(this);
		readerGame.setDaemon(true);
		readerGame.start();
	}

	@Override
	public void windowClosed(WindowEvent evt) {

		quit = true;
		try {
			outGame.flush();
			outGame.close();
			inGame.close();
		} catch (Exception e) {

		}

		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent evt) {
		frame.setVisible(false); // default behaviour of JFrame
		frame.dispose();
	}

	public void run() {

		while (Thread.currentThread() == readerUser) {

			try {
				String userInput = inUserBufferedReader.readLine();
				System.err.println("User input: " + userInput);
				userInputBuffer.add(userInput.substring(1));
				currentLineNum = currentLineNum + 1;

				if (quit) {
					return;
				}

			} catch (IOException io) {

			}
		}

		while (Thread.currentThread() == readerGame) {

			try {
				if (inGameBufferedReader.ready()) {
					waitingUserInput = false;
					String gameInput = inGameBufferedReader.readLine();
					System.err.println("Game input: " + gameInput);
					textArea.setFont(gameFont);

					//first count the new lines that exist in the game input as is
					String[] lines = gameInput.split("\n");
					System.err.println("Lines of " + Arrays.toString(lines) + ": " + lines.length);
					currentLineNum = currentLineNum + lines.length;

					//then find the full sentences
					String[] sentences = gameInput.split("\\.");
					if (sentences.length > 1) {
						System.err.println("Sentences of " + Arrays.toString(sentences) + ":: " + sentences.length);
						for (int i = 0; i < sentences.length; i++) {
							if (i == 0) {
								textArea.append(sentences[i] + ".\n");
							} else {
								textArea.append(sentences[i].substring(1) + ".\n");
							}
						}
						textArea.append("\n");
						currentLineNum = currentLineNum + sentences.length;
					} else {
						textArea.append(gameInput);
						textArea.append("\n");
					}

				} else if (!waitingUserInput) {
					waitingUserInput = true;
					textArea.append(">");
					textArea.setFont(userFont);

					try {
						System.err.println("Text area: current line " + currentLineNum);
						int nextLineOffset = textArea.getLineStartOffset(currentLineNum);
						System.err.println("Text area: current line " + currentLineNum + " and next line offset " + nextLineOffset);
						textArea.setCaretPosition(nextLineOffset + 1);
					} catch (BadLocationException e) {
						System.err.println("Exception while trying to place caret after game input");
					}
				}

				if (quit) {
					return;
				}

			} catch (IOException io) {

			}
		}
	}

	//the game engine main thread may need to snatch the output stream in order to print a message from the game itself
	public void printGame(String gameString) {

		formattedOutGame.println(gameString);
		formattedOutGame.flush();
	}

	public String getNextUserInput() {

		return userInputBuffer.poll();
	}

	private static final class TextAreaStreamer extends InputStream implements KeyListener {

		private JTextArea ta;
		private String str = null;
		private int pos = 0;

		public TextAreaStreamer(JTextArea jta) {
			ta = jta;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			System.err.println(e.getKeyCode() + " pressed");
			if (e.getKeyCode() == e.VK_ENTER) {

				int endPos = ta.getCaret().getMark();
				int startPos = ta.getText().substring(0, endPos - 1).lastIndexOf('\n') + 1;
				try {
					str = ta.getText(startPos, endPos - startPos + 1);
					System.err.println("ENTER key pressed: " + str.substring(0, str.length() - 1)); //omit trailing new line character
				} catch (BadLocationException ex) {
				}

				pos = 0;

				synchronized (this) {
					this.notifyAll();
				}
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public int read() {
			//test if the available input has reached its end
			//and the EOS should be returned
			if(str != null && pos == str.length()){
				str = null;
				//this is supposed to return -1 on "end of stream"
				//but I'm having a hard time locating the constant
				return java.io.StreamTokenizer.TT_EOF;
			}
			//no input available, block until more is available because that's
			//the behavior specified in the Javadocs
			while (str == null || pos >= str.length()) {
				try {
					//according to the docs read() should block until new input is available
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			//read an additional character, return it and increment the index
			return str.charAt(pos++);
		}

	}
}
