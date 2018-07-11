/**
 * @(#)Piece.java
 *
 *
 * @author 
 * @version 1.00 2015/2/12
 */
 
 import java.awt.*;
 import java.io.File;
 import javax.imageio.*;
 
public class Piece implements Cloneable, Comparable<Piece>
{
	public char piece, white;
	public boolean hasMoved;
	public int row, col, value;
	public Image img;
	
	public static Image WR;
	public static Image WN;
	public static Image WB;
	public static Image WQ;
	public static Image WK;
	public static Image WP;
	public static Image BR;
	public static Image BN;
	public static Image BB;
	public static Image BQ;
	public static Image BK;
	public static Image BP;
	
	public static final Piece empty = new Piece();
	
	public Piece()
	{
		piece = ' ';
		white = ' ';
		row = -1;
		col = -1;
		value = 0;
		hasMoved = false;
	}
	
	public Piece(char p, char w, int r, int c)
	{
		if(r < 0 || r > 7 || c < 0 || c > 7)
		{
			piece = ' ';
			white = ' ';
			row = -1;
			col = -1;
			value = 0;
		}
		else
		{
			piece = p;
			white = w;
			row = r;
			col = c;
		
			if(piece != ' ')
			{
				if(white == 'W')
					switch(piece)
					{
						case 'R': img = WR; value = 5; break;
						case 'N': img = WN; value = 3; break;
						case 'B': img = WB; value = 3; break;
						case 'Q': img = WQ; value = 9; break;
						case 'K': img = WK; value = 0; break;
						case 'P': img = WP; value = 1; break;
					}
				else
					switch(piece)
					{
						case 'R': img = BR; value = 5; break;
						case 'N': img = BN; value = 3; break;
						case 'B': img = BB; value = 3; break;
						case 'Q': img = BQ; value = 9; break;
						case 'K': img = BK; value = 0; break;
						case 'P': img = BP; value = 1; break;
					}
			}
		}
		hasMoved = false;
	}
	
	public static void loadImages()
	{
		try
		{
			WR = ImageIO.read(new File("WR.png"));
			WN = ImageIO.read(new File("WN.png"));
			WB = ImageIO.read(new File("WB.png"));
			WQ = ImageIO.read(new File("WQ.png"));
			WK = ImageIO.read(new File("WK.png"));
			WP = ImageIO.read(new File("WP.png"));
			BR = ImageIO.read(new File("BR.png"));
			BN = ImageIO.read(new File("BN.png"));
			BB = ImageIO.read(new File("BB.png"));
			BQ = ImageIO.read(new File("BQ.png"));
			BK = ImageIO.read(new File("BK.png"));
			BP = ImageIO.read(new File("BP.png"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public char getPieceType()
	{
		return piece;
	}
	
	public char getWhite()
	{
		return white;
	}
	
	public Object clone()
	{
		Piece p = new Piece(piece, white, row, col);
		if(hasMoved)
			p.hasMoved = true;
		return p;
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Piece))
			return false;
		if(((Piece)obj).hashCode() == hashCode())
			return true;
		return false;
	}
	
	public int compareTo(Piece p)
	{
		if(p == null)
			return -1;
		if(p.hashCode() > hashCode())
			return 1;
		if(p.equals(this))
			return 0;
		return -1;	
	}
	
	public int hashCode()
	{
		int hash = row;
		hash <<= 3;
		hash |= col;
		hash <<= 16;
		hash |= piece;
		return hash;
	}
	
	public String toString()
	{
		return "Piece = " + piece + " row = " + row + " col = " + col;
	}
}
