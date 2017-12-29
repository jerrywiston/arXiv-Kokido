import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;


public class TagLabel extends JLabel
{
	private static final long serialVersionUID = 1L;
	private boolean isSelected;
	
	
	/*--------------------------------
	Constructor
	--------------------------------*/
	public TagLabel()
	{
		super();
		
		isSelected = false;
		setForeground(Color.white);
		setBorder(BorderFactory.createLineBorder(Color.white));
		setFont(new Font("Verdana", Font.PLAIN, 18));
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				setSelected(!isSelected);
			}
		});
	}
	
	
	public TagLabel(String txt)
	{
		super(txt);
		
		isSelected = false;
		setForeground(Color.white);
		setBorder(BorderFactory.createLineBorder(Color.white));
		setFont(new Font("Verdana", Font.PLAIN, 18));
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				setSelected(!isSelected);
			}
		});
	}
	
	
	/*--------------------------------
	Refresh
	--------------------------------*/
	public void refresh()
	{
		Container parent = getParent();
		if(parent == null)
		{
			revalidate();
			repaint();
		}
		else 
		{
			Container ancestor = parent.getParent();
			while(ancestor != null)
			{
				parent = ancestor;
				ancestor = parent.getParent();
			}
			parent.revalidate();
			parent.repaint();
		}
	}
	
	
	/*--------------------------------
	Access isSelected
	--------------------------------*/
	public void setSelected(boolean s)
	{
		isSelected = s;
		if(isSelected)
		{
			setForeground(Color.orange);
			setBorder(BorderFactory.createLineBorder(Color.orange));
		}
		else
		{
			setForeground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.white));
		}
		
		refresh();
	}
	
	
	public boolean getSelected()
	{
		return isSelected;
	}
}
