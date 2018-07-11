/**
 * @(#)Chess2.java
 *
 *
 * @author Conner Newman
 * @version 1.00 2015/2/12
 */

import java.util.ArrayList;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Chess2 extends JFrame
{
	//private BufferedImage b;
	private JPanel main;
	private MainPanel panel;
	private OptionPanel options;
	private JLabel check;
	private JButton reset;
	private JButton undo;
	private JButton save;
	private JButton load;
		
	private Thread thread;
	private final int tick = 25;
	private int t;
	
	private Piece[][] board;
	private final Piece empty = Piece.empty;
	private boolean click;
	private int r1, r2, c1, c2;
	private int movesSinceLastCapture;
	private int castle;
	public static final int CASTLE_BOTTOM_LEFT = 1;
	public static final int CASTLE_BOTTOM_RIGHT = 2;
	public static final int CASTLE_TOP_LEFT = 3;
	public static final int CASTLE_TOP_RIGHT = 4;
	private boolean whiteMove;
	private ArrayList<Move> moveList;
	
	private Chess2()
	{
		check = new JLabel("");
		try
		{
			Image i = ImageIO.read(new File("BK.png"));
			i = i.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			check.setIcon(new ImageIcon(i));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		createNewBoard();
		setIconImage(board[7][3].img);
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Chess 2");
		t = 0;
		
		//b = null;
		main = new JPanel();
		main.setLayout(new BorderLayout());
		panel = new MainPanel();
		reset = new JButton("New Game");
		undo = new JButton("Undo Move");
		save = new JButton("Save Game");
		load = new JButton("Load Game");
		options = new OptionPanel();
		options.setLayout(new GridLayout());
		main.add(panel, BorderLayout.CENTER);
		main.add(options, BorderLayout.SOUTH);
		options.add(check);
		options.add(undo);
		options.add(save);
		options.add(load);
		options.add(reset);
		
		setContentPane(main);
		setVisible(true);
		
		thread = new Thread(panel);
		thread.start();
		
		panel.repaint();
	}
	
	public static void main(String[] args)
	{
		Piece.loadImages();
		new Chess2();
	}
	
	private void createNewBoard()
	{
		check.setVisible(false);
		
		whiteMove = true;
		click = true;
		r1 = r2 = c1 = c2 = -1;
		castle = 0;
		movesSinceLastCapture = 0;
		
		moveList = new ArrayList<Move>();
		board = new Piece[8][8];
		for(int i = 0; i < 8; ++i)
			for(int k = 0; k < 8; ++k)
				board[i][k] = empty;
		
		board[0][0] = new Piece('R', 'B', 0, 0);
		board[0][1] = new Piece('N', 'B', 0, 1);
		board[0][2] = new Piece('B', 'B', 0, 2);
		board[0][3] = new Piece('Q', 'B', 0, 3);
		board[0][4] = new Piece('K', 'B', 0, 4);
		board[0][5] = new Piece('B', 'B', 0, 5);
		board[0][6] = new Piece('N', 'B', 0, 6);
		board[0][7] = new Piece('R', 'B', 0, 7);
		
		board[7][0] = new Piece('R', 'W', 7, 0);
		board[7][1] = new Piece('N', 'W', 7, 1);
		board[7][2] = new Piece('B', 'W', 7, 2);
		board[7][3] = new Piece('Q', 'W', 7, 3);
		board[7][4] = new Piece('K', 'W', 7, 4);
		board[7][5] = new Piece('B', 'W', 7, 5);
		board[7][6] = new Piece('N', 'W', 7, 6);
		board[7][7] = new Piece('R', 'W', 7, 7);
		
		for(byte b = 0; b < 8; ++b)
		{
			board[1][b] = new Piece('P', 'B', 1, b);
			board[6][b] = new Piece('P', 'W', 6, b);
		}
	}
	
	private void parseMove()
	{
		if(willIBeInCheckIfIMakeThisMove())
		{
			panel.h2 = false;
			return;
		}
		if(canMove(true) && movesSinceLastCapture <= 50)
		{
			moveList.add(new Move(r1, c1, r2, c2, board[r1][c1].piece, board[r2][c2].piece, board[r1][c1].hasMoved));	
			if(!(board[r2][c2] == empty))
				movesSinceLastCapture = 0;
			board[r1][c1].row = r2;
			board[r1][c1].col = c2;
			board[r1][c1].hasMoved = true;
			board[r2][c2] = board[r1][c1];
			board[r1][c1] = empty;
			
			if(castle == 0)
			{
			}
			else if(castle == CASTLE_BOTTOM_LEFT)
			{
				board[7][0].hasMoved = true;
				board[7][3] = board[7][0];
				board[7][0] = empty;
			}
			else if(castle == CASTLE_BOTTOM_RIGHT)
			{
				board[7][7].hasMoved = true;
				board[7][5] = board[7][7];
				board[7][7] = empty;
			}
			else if(castle == CASTLE_TOP_LEFT)
			{
				board[0][0].hasMoved = true;
				board[0][3] = board[0][0];
				board[0][0] = empty;
			}
			else if(castle == CASTLE_TOP_RIGHT)
			{
				board[0][7].hasMoved = true;
				board[0][5] = board[0][7];
				board[0][7] = empty;
			}
			castle = 0;
			
			if(board[r2][c2].piece == 'P' && (r2 == 0 || r2 == 7))
			{
				char team = board[r2][c2].white;
				char piece = 'Q';
				JOptionPane op = new JOptionPane("Select a piece:");
				Object[] options = {"Rook", "Knight", "Bishop", "Queen"};
				op.setOptions(options);
				JDialog d = op.createDialog(this, "Congratulations!");
				d.setVisible(true);
				String s = (String)op.getValue();
				if(s == null)
				{
				}
				else if(s.equals("Rook"))
					piece = 'R';
				else if(s.equals("Knight"))
					piece = 'N';
				else if(s.equals("Bishop"))
					piece = 'B';
				else if(s.equals("Queen"))
					piece = 'Q';
				board[r2][c2] = new Piece(piece, team, r2, c2);
			}
			
			if(check())
			{
				check.setText("CHECK!");
				check.setVisible(true);
			}
			else
				check.setVisible(false);
			
			if(getAllMoves(!whiteMove).size() == 0)
			{
				if(check())
					check.setText("CHECKMATE!");
				else
					check.setText("Stalemate");
				check.setVisible(true);
			}
			
			if(++movesSinceLastCapture >= 50)
			{
				check.setText("Stalemate");
				check.setVisible(true);
			}
			
			whiteMove = !whiteMove;
		}
	}
	
	private void playRandomMove()
	{
		ArrayList<Move> moves = getAllMoves(whiteMove);
		if(moves.size() == 0)
			return;
		playMove(moves.get((int)Math.random() * moves.size()));
	}
	
	private void playBestMove()
	{
		Piece[][] savedBoardInstance = cloneBoard(board);
		
		ArrayList<Move> moves1 = getAllMoves(whiteMove);
		if(moves1.size() == 0)
			return;
		Move bestMove = moves1.get(0);
		int score = 0;
		int highestResponseScore = -1000;
		for(int i1 = 0; i1 < moves1.size(); ++i1)
		{
			Move m1 = moves1.get(i1);
			score = board[m1.row2][m1.col2].value;
			doMove(m1, board);
			Piece[][] boardInstance2 = cloneBoard(board);
			ArrayList<Move> responses1 = getAllMoves(whiteMove);
			for(int i2 = 0; i2 < responses1.size(); ++i2)
			{
				board = cloneBoard(boardInstance2);
				Move m2 = responses1.get(i2);
				if(score - board[m2.row2][m2.col2].value > highestResponseScore)
				{
					highestResponseScore = score - board[m2.row2][m2.col2].value;
					bestMove = m1;
				}
				if(score - board[m2.row2][m2.col2].value == highestResponseScore && Math.random() < .5)
				{
					highestResponseScore = score - board[m2.row2][m2.col2].value;
					bestMove = m1;
				}
			}
		}
		board = savedBoardInstance;
		whiteMove = false;
		playMove(bestMove);
		whiteMove = true;
	}
	
	private void playAwfulMove()
	{
		Piece[][] tempBoard1 = cloneBoard(board);
		for(int i = 0; i < 8; ++i)
		{
			for(int k = 0; k < 8; ++k)
			{
				System.out.print(tempBoard1[i][k].piece);
			}
			System.out.println();
		}
		ArrayList<Move> moves = getAllMoves(whiteMove);
		Move bestMove = new Move(0, 0, 0, 0, ' ', true);
		int iterations = 0;
		int score = 0;
		for(int i1 = 0; i1 < moves.size(); ++i1)
		{
			Move m1 = moves.get(i1);
			int tempScore = board[m1.row2][m1.col2].value;
			doMove(m1, board);
			ArrayList<Move> moves2 = getAllMoves(whiteMove);
			for(int i2 = 0; i2 < moves2.size(); ++i2)
			{
				Move m2 = moves2.get(i2);
				tempScore -= board[m2.row2][m2.col2].value;
				doMove(m2, board);
				ArrayList<Move> moves3 = getAllMoves(whiteMove);
				Piece[][] tempBoard2 = cloneBoard(tempBoard1);
				for(int i3 = 0; i3 < moves3.size(); ++i3)
				{
					Move m3 = moves3.get(i3);
					tempScore += board[m3.row2][m3.col2].value;
					doMove(m3, board);
					ArrayList<Move> moves4 = getAllMoves(whiteMove);
					Piece[][] tempBoard3 = cloneBoard(tempBoard2);
					for(int i4 = 0; i4 < moves4.size(); ++i4)
					{
						Move m4 = moves4.get(i4);
						tempScore -= board[m4.row2][m4.col2].value;
						if(tempScore > score)
						{
							bestMove = m4;
							score = tempScore;
						}
						++iterations;
					}
				}
			}
		}
		board = tempBoard1;
		whiteMove = true;
		playMove(bestMove);
		System.out.println(iterations + " iterations, score: " + score + " best move: " + bestMove);
	}
	
	private boolean canMove(boolean mainMove)
	{
		try
		{
			if(board[r1][c1] == null || board[r2][c2] == null)
				return false;
		}
		catch(Exception e)
		{
			System.out.println("" + r1 + c1 + r2 + c2);
		}
		
		Piece p = board[r1][c1];
		Piece p2 = board[r2][c2];
		char piece = p.piece;
		char team = p.white;
		
		if(p.white == p2.white)
			return false;
		
		if(whiteMove ^ team == 'W')
			return false;
			
		if(piece == 'P')
		{
			if(c1 == c2 && p2 != empty)
				return false;
			if(!(c1 - c2 == 0 || c1 - c2 == 1 || c2 - c1 == 1))
				return false;
			if(!p.hasMoved)
			{
				if(team == 'B' && !(r2 - r1 == 1 || r2 - r1 == 2))
					return false;
				else if(team == 'W' && !(r1 - r2 == 1 || r1 - r2 == 2))
					return false;
				
				if(r1 - r2 == 2 || r1 - r2 == -2)
				{
					if(c1 != c2)
						return false;
					if(p2 != empty)
						return false;
					if(board[r1 + (r2 - r1) / 2][c2] != empty)
						return false;
				}
				else if(c1 != c2 && p2 == empty)
						return false;
			}
			else
			{
				if(team == 'B' && r2 - r1 != 1)
					return false;
				else if(team == 'W' && r1 - r2 != 1)
					return false;
				if(c2 - c1 == 1 || c1 - c2 == 1)
					if(p2 == empty)
					{
						Move lastMove = moveList.get(moveList.size() - 1);
						if(lastMove.col1 != c2 || !(lastMove.row1 - lastMove.row2 == 2 || lastMove.row2 - lastMove.row1 == 2) || !(lastMove.piece == 'P') || !(lastMove.row2 - r2 == 1 || r2 - lastMove.row2 == 1))
							return false;
						if(mainMove && !(willIBeInCheckIfIMakeThisMove()))
							board[lastMove.row2][lastMove.col2] = empty;
					}
			}
		}
		else if(piece == 'R')
		{
			if(c1 != c2 && r1 != r2)
				return false;
			if(c1 == c2)
			{
				if(r1 < r2)
					for(int r = r1 + 1; r < r2; r++)
					{
						if(!(board[r][c1] == empty))
							return false;
					}
				else
					for(int r = r1 - 1; r > r2; r--)
						if(!(board[r][c1] == empty))
							return false;
			}
			else
				if(c1 < c2)
					for(int c = c1 + 1; c < c2; c++)
					{
						if(!(board[r1][c] == empty))
							return false;
					}
				else
					for(int c = c1 - 1; c > c2; c--)
						if(!(board[r1][c] == empty))
							return false;
		}
		else if(piece == 'N')
		{
			if(!(r1 - r2 == 2 || r2 - r1 == 2 || c1 - c2 == 2 || c2 - c1 == 2))
				return false;
			if(!(r1 - r2 == 1 || r2 - r1 == 1 || c1 - c2 == 1 || c2 - c1 == 1))
				return false;
		}
		else if(piece == 'B')
		{
			int dc = 1;
			int dr = 1;
			if(!(r1 - r2 == c1 - c2 || r1 - r2 == c2 - c1))
				return false;
			if(r1 > r2)
				dr = -1;
			if(c1 > c2)
				dc = -1;
			for(int r = r1 + dr, c = c1 + dc; r != r2; r += dr, c += dc)
				if(!(board[r][c] == empty))
					return false;
		}
		else if(piece == 'K')
		{
			if((c1 - c2 == 2 || c2 - c1 == 2) && mainMove)
			{
				if(p.hasMoved || r1 != r2)
					return false;
				if(team == 'W')
				{
					if(c2 == 2)
					{
						if(board[7][0].hasMoved)
							return false;
						if(board[7][3] != empty || board[7][1] != empty)
							return false;
						castle = CASTLE_BOTTOM_LEFT;
						return true;
					}
					else if(c2 == 6)
					{
						if(board[7][7].hasMoved)
							return false;
						if(board[7][5] != empty)
							return false;
						castle = CASTLE_BOTTOM_RIGHT;
						return true;
					}
				}
				else if(team == 'B')
				{
					if(c2 == 2)
					{
						if(board[0][0].hasMoved)
							return false;
						if(board[0][3] != empty || board[0][1] != empty)
							return false;
						castle = CASTLE_TOP_LEFT;
						return true;
					}
					else if(c2 == 6)
					{
						if(board[0][7].hasMoved)
							return false;
						if(board[0][5] != empty)
							return false;
						castle = CASTLE_TOP_RIGHT;
						return true;
					}
				}
			}
			if(r1 - r2 > 1 || r2 - r1 > 1 || c1 - c2 > 1 || c2 - c1 > 1)
				return false;
		}
		else if(piece == 'Q')
		{
			if(c1 == c2)
			{
				if(r1 < r2)
					for(int r = r1 + 1; r < r2; r++)
					{
						if(!(board[r][c1] == empty))
							return false;
					}
				else
					for(int r = r1 - 1; r > r2; r--)
						if(!(board[r][c1] == empty))
							return false;
			}
			else if(r1 == r2)
			{
				if(c1 < c2)
					for(int c = c1 + 1; c < c2; c++)
					{
						if(!(board[r1][c] == empty))
							return false;
					}
				else
					for(int c = c1 - 1; c > c2; c--)
						if(!(board[r1][c] == empty))
							return false;
			}
			else
			{
				int dc = 1;
				int dr = 1;
				if(!(r1 - r2 == c1 - c2 || r1 - r2 == c2 - c1))
					return false;
				if(r1 > r2)
					dr = -1;
				if(c1 > c2)
					dc = -1;
				for(int r = r1 + dr, c = c1 + dc; r != r2; r += dr, c += dc)
					if(!(board[r][c] == empty))
						return false;
			}
		}
		else
			return false;		
		return true;
	}
	
	private boolean check()
	{
		byte kr, kc;
		kr = kc = -1;
		for(byte r = 0; r < 8; ++r)
		{
			for(byte c = 0; c < 8; ++c)
			{
				if(board[r][c].piece != 'K')
					continue;
				if(!whiteMove && board[r][c].white == 'W')
				{
					kr = r;
					kc = c;
					break;
				}
				else if(whiteMove && board[r][c].white == 'B')
				{
					kr = r;
					kc = c;
					break;
				}
			}
			if(kr != -1)
				break;
		}
		
		int r1t = r1;
		int c1t = c1;
		int r2t = r2;
		int c2t = c2;
		r2 = kr;
		c2 = kc;
		
		for(r1 = 0; r1 < 8; ++r1)
			for(c1 = 0; c1 < 8; ++c1)
				if(canMove(false))
				{
					r1 = r1t;
					c1 = c1t;
					r2 = r2t;
					c2 = c2t;
					return true;
				}
		r1 = r1t;
		c1 = c1t;
		r2 = r2t;
		c2 = c2t;
		return false;
	}

	private boolean willIBeInCheckIfIMakeThisMove()
	{
		//to do: prevent castling in/through check
		Piece[][] tempBoard = new Piece[8][8];
		for(byte r = 0; r < 8; ++r)
			for(byte c = 0; c < 8; ++c)
			{	
				if(board[r][c] == empty)
					tempBoard[r][c] = empty;
				else
					tempBoard[r][c] = (Piece)board[r][c].clone();
			}
		board[r1][c1].row = r2;
		board[r1][c1].col = c2;
		board[r1][c1].hasMoved = true;
		board[r2][c2] = board[r1][c1];
		board[r1][c1] = empty;
		
		int kr, kc;
		kr = kc = -1;
		for(byte r = 0; r < 8; ++r)
		{
			for(byte c = 0; c < 8; ++c)
			{
				if(board[r][c].piece != 'K')
					continue;
				if(whiteMove && board[r][c].white == 'W')
				{
					kr = r;
					kc = c;
					break;
				}
				else if(!whiteMove && board[r][c].white == 'B')
				{
					kr = r;
					kc = c;
					break;
				}
			}
			if(kr != -1)
				break;
		}
		int r1t = r1;
		int c1t = c1;
		int r2t = r2;
		int c2t = c2;
		r2 = kr;
		c2 = kc;
		
		whiteMove = !whiteMove;
		for(r1 = 0; r1 < 8; ++r1)
			for(c1 = 0; c1 < 8; ++c1)
				if(canMove(false))
				{
					r1 = r1t;
					c1 = c1t;
					r2 = r2t;
					c2 = c2t;
					board = tempBoard;
					if(whiteMove)
						whiteMove = false;
					else
						whiteMove = true;
					return true;
				}
		r1 = r1t;
		c1 = c1t;
		r2 = r2t;
		c2 = c2t;
		board = tempBoard;
		whiteMove = !whiteMove;
		return false;
	}
	
	private ArrayList<Move> getAllMoves(boolean white)
	{
		boolean whiteTemp = whiteMove;
		whiteMove = white;
		ArrayList<Move> moves = new ArrayList<Move>();
		int r1t = r1;
		int c1t = c1;
		int r2t = r2;
		int c2t = c2;
		for(r1 = 0; r1 < 8; ++r1)
			for(c1 = 0; c1 < 8; ++c1)
				if(board[r1][c1].white == 'W' && white || board[r1][c1].white == 'B' && !white)
					for(r2 = 0; r2 < 8; ++r2)
						for(c2 = 0; c2 < 8; ++c2)
							if(canMove(false) && !willIBeInCheckIfIMakeThisMove())
								moves.add(new Move(r1, c1, r2, c2, board[r1][c1].piece, board[r1][c1].hasMoved));
		r1 = r1t;
		c1 = c1t;
		r2 = r2t;
		c2 = c2t;
		whiteMove = whiteTemp;
		return moves;
	}
	
	private void playMove(Move m)
	{
		r1 = m.row1;
		c1 = m.col1;
		r2 = m.row2;
		c2 = m.col2;
		parseMove();
	}
	
	private void doMove(Move m, Piece[][] b)
	{
		r1 = m.row1;
		c1 = m.col1;
		r2 = m.row2;
		c2 = m.col2;
		if(b[r2][c2].piece == 'K')
		{
			System.out.println("we have managed to target a king");
			return;
		}
		b[r1][c1].row = r2;
		b[r1][c1].col = c2;
		b[r1][c1].hasMoved = true;
		b[r2][c2] = b[r1][c1];
		b[r1][c1] = empty;
		
		/*if(castle == 0)
		{
		}
		else if(castle == CASTLE_BOTTOM_LEFT)
		{
			b[7][0].hasMoved = true;
			b[7][3] = b[7][0];
			b[7][0] = empty;
		}
		else if(castle == CASTLE_BOTTOM_RIGHT)
		{
			b[7][7].hasMoved = true;
			b[7][5] = b[7][7];
			b[7][7] = empty;
		}
		else if(castle == CASTLE_TOP_LEFT)
		{
			b[0][0].hasMoved = true;
			b[0][3] = b[0][0];
			b[0][0] = empty;
		}
		else if(castle == CASTLE_TOP_RIGHT)
		{
			b[0][7].hasMoved = true;
			b[0][5] = b[0][7];
			b[0][7] = empty;
		}
		castle = 0;*/
		
		if(b[r2][c2].piece == 'P' && (r2 == 0 || r2 == 7))
			b[r2][c2] = new Piece('Q', b[r2][c2].white, r2, c2);
		
		whiteMove = !whiteMove;
	}
	
	private void undoLastMove()
	{
		if(moveList.size() == 0)
			return;
		Move move = moveList.get(moveList.size() - 1);
		board[move.row1][move.col1] = board[move.row2][move.col2];
		if(move.captured != ' ')
		{
			char c = 'B';
			if(whiteMove)
				c = 'W';
			board[move.row2][move.col2] = new Piece(move.captured, c, move.row2, move.col2);
		}
		else
			board[move.row2][move.col2] = empty;
		if(move.piece == 'K')
		{
			if(move.col2 - move.col1 == 2)
				if(!whiteMove)
				{
					board[7][7] = new Piece('R', 'W', 7, 0);
					board[7][5] = empty;
				}
				else
				{
					board[0][7] = new Piece('R', 'B', 0, 0);
					board[0][5] = empty;
				}
			else if(move.col1 - move.col2 == 2)
				if(!whiteMove)
				{
					board[7][0] = new Piece('R', 'W', 7, 7);
					board[7][3] = empty;
				}
				else
				{
					board[0][0] = new Piece('R', 'B', 0, 7);
					board[0][3] = empty;
				}
		}
		else if(move.piece == 'P' && move.col1 != move.col2 && move.captured == ' ')
		{
			if(board[move.row2][move.col2].white == 'W')
			{
				board[move.row2 - 1][move.col2] = new Piece('P', 'W', move.row2 - 1, move.col2);
				movesSinceLastCapture = 0;
				board[move.row2 - 1][move.col2].hasMoved = true;
			}
			else
			{
				board[move.row2 + 1][move.col2] = new Piece('P', 'B', move.row2 + 1, move.col2);
				movesSinceLastCapture = 0;
				board[move.row2 + 1][move.col2].hasMoved = true;
			}
		}
		
		board[move.row1][move.col1].hasMoved = move.moved;
		
		panel.h1 = panel.h2 = false;
		whiteMove = !whiteMove;
		--movesSinceLastCapture;
		moveList.remove(moveList.size() - 1);
		click = true;
		
		if(check())
		{
			check.setText("CHECK!");
			check.setVisible(true);
		}
		else
			check.setVisible(false);
		
		if(getAllMoves(!whiteMove).size() == 0)
		{
			if(check())
				check.setText("CHECKMATE!");
			else
				check.setText("Stalemate");
			check.setVisible(true);
		}
	}
	
	private Piece[][] cloneBoard(Piece[][] boardToClone)
	{
		Piece[][] tempBoard = new Piece[8][8];
		for(byte r = 0; r < 8; ++r)
			for(byte c = 0; c < 8; ++c)
			{
				if(boardToClone[r][c] == empty)
					tempBoard[r][c] = empty;
				else
					tempBoard[r][c] = (Piece)boardToClone[r][c].clone();
			}
		return tempBoard;
	}
	
	private class Move implements Comparable<Move>
	{
		public int row1, col1, row2, col2;
		public char piece, captured;
		public boolean moved;
		
		private Move(int r, int c, int r2, int c2, char p, boolean m)
		{
			row1 = r;
			col1 = c;
			row2 = r2;
			col2 = c2;
			piece = p;
			captured = ' ';
			moved = m;
		}
		
		private Move(int r, int c, int r2, int c2, char p, char cap, boolean m)
		{
			row1 = r;
			col1 = c;
			row2 = r2;
			col2 = c2;
			piece = p;
			captured = cap;
			moved = m;
		}
		
		private Move(String s)
		{
			Scanner scan = new Scanner(s);
			row1 = scan.nextInt();
			col1 = scan.nextInt();
			row2 = scan.nextInt();
			col2 = scan.nextInt();
			if(scan.nextBoolean())
				moved = true;
			else
				moved = false;
			String chars = scan.next();
			piece = chars.charAt(0);
			if(chars.length() == 2)
				captured = chars.charAt(1);
			scan.close();
		}
		
		public String toString()
		{
			return row1 + " " + col1 + " " + row2 + " " + col2 + " " + moved + " " + piece + " " + captured;
		}
		
		public boolean equals(Object obj)
		{
			if(obj == null || !(obj instanceof Piece))
				return false;
			Move move = (Move)obj;
			if(row1 == move.row1 && col1 == move.col1 && row2 == move.row2 && col2 == move.col2 && piece == move.piece && moved == move.moved)
				return true;
			return false;
		}
		
		public int compareTo(Move m)
		{
			if(m == null)
				return -1;
			if(m.row1 < row1)
				return -1;
			if(m.row1 > row1)
				return 1;
			if(m.row1 == row1)
			{
				if(m.col1 > col1)
					return 1;
				else if(m.col2 < col2)
					return -1;
			}
			return 0;
		}
	}
	
	private class MainPanel extends JPanel implements Runnable, MouseListener, MouseMotionListener
	{
		int x, y;
		int w, h;
		private boolean h1, h2;
		Color white = new Color(230, 230, 230);
		Color black = new Color(100, 100, 100);
		Color highlighted1 = new Color(0, 200, 200);
		Color highlighted2 = new Color(0, 200, 100);
		private BufferedImage b;
		
		private MainPanel()
		{
			super();
			addMouseListener(this);
			addMouseMotionListener(this);
			h1 = h2 = false;
		}
		
		public void update(Graphics g)
		{
			paintComponent(g);
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent((Graphics2D)g);
			
			w = getWidth();
			h = getHeight();

			Graphics2D g2 = (Graphics2D)g;
			
			b = (BufferedImage)createImage(w, h);
			g = b.createGraphics();
			
			g.setColor(white);
			g.fillRect(0, 0, w, h);
			
			for(int r = 0; r < 8; ++r)
				for(int c = 0; c < 8; ++c)
				{
					if((c % 2) != (r % 2))
						g.setColor(black);
					else
						g.setColor(white);
					g.fillRect(r * w / 8, c * h / 8, w / 8, h / 8);
				}
			
			if(h1)
			{
				g.setColor(highlighted1);	
				g.fillRect(c1 * w / 8, r1 * h / 8, w / 8, h / 8);
			}			
			if(h2)
			{
				g.setColor(highlighted2);
				g.fillRect(c2 * w / 8, r2 * h / 8, w / 8, h / 8);
			}

			for(int r = 0; r < 8; ++r)
				for(int c = 0; c < 8; ++c)
					if(board[r][c] != null && board[r][c].img != null)
						g.drawImage(board[r][c].img, c * w / 8, r * h / 8, this);
			
			g2.drawImage(b, null, 0, 0);
		}

		public void mouseExited(MouseEvent e)
		{
		}
		
		public void mouseEntered(MouseEvent e)
		{
		}
		
		public void mousePressed(MouseEvent e)
		{
			x = e.getX();
			y = e.getY();
			
			if(click)
			{
				if(board[y * 8 / h][x * 8 / w].white == 'W' && whiteMove || board[y * 8 / h][x * 8 / w].white == 'B' && !whiteMove)
				{
					r1 = y * 8 / h;
					c1 = x * 8 / w;
					h1 = true;
					h2 = false;
					click = false;
				}
			}
			else
			{
				r2 = y * 8 / h;
				c2 = x * 8 / w;
				if(canMove(false) && !willIBeInCheckIfIMakeThisMove())
				{
					click = true;
					h2 = true;
				}
				else
				{
					if(board[y * 8 / h][x * 8 / w].white == 'W' && whiteMove || board[y * 8 / h][x * 8 / w].white == 'B' && !whiteMove)
					{
						r1 = y * 8 / h;
						c1 = x * 8 / w;
						click = false;
					}
					else
					{
						h1 = false;
						click = true;
					}
				}
				parseMove();
			}
		}
		
		public void mouseReleased(MouseEvent e)
		{
		}
		
		public void mouseClicked(MouseEvent e)
		{
		}
		
		public void mouseMoved(MouseEvent e)
		{
		}
		
		public void mouseDragged(MouseEvent e)
		{
		}
		
		public void run()
		{
			setFocusable(true);
			requestFocus();
			try
			{
				while(true)
				{
					++t;
					//if(!whiteMove)
					//	playBestMove();
					panel.repaint();
					Thread.sleep(tick);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("Crash time: " + t / 40 + " seconds, (t=" + t + ")");
				return;
			}
		}
	}
	
	private class OptionPanel extends JPanel implements ActionListener
	{
		private OptionPanel()
		{
			reset.addActionListener(this);
			save.addActionListener(this);
			load.addActionListener(this);
			undo.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			if(source == reset)
				createNewBoard();
			else if(source == undo)
				undoLastMove();
			else if(source == save)
			{
				JFileChooser j = new JFileChooser();
				j.setFileFilter(new FileNameExtensionFilter("Save Games", "txt"));
				j.showSaveDialog(this);
				File saveGame = j.getSelectedFile();
				try
				{
					PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(saveGame)));
					p.println("Conner's Chess2 save file");
					for(Move m : moveList)
						p.println(m);
					p.close();
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
				}
			}
			else if(source == load)
			{
				JFileChooser j = new JFileChooser();
				j.setFileFilter(new FileNameExtensionFilter("Save Games", "txt"));
				j.showOpenDialog(this);
				File gameToLoad = j.getSelectedFile();
				if(gameToLoad != null)
					try
					{
						createNewBoard();
						Scanner s = new Scanner(gameToLoad);
						moveList.clear();
						if(!s.nextLine().equals("Conner's Chess2 save file"))
						{
							s.close();
							return;
						}
						while(s.hasNextLine())
							playMove(new Move(s.nextLine()));
						s.close();
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
			}
		}
	}
}
