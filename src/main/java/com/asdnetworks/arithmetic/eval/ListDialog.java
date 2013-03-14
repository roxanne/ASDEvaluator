package asd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
/**
 * The source code for this class is from "The JFC Swing Tutorial,
 * by Kathy Walrath and Mary Campione, Addison-Wesley, 1999.
 *
 * Use this modal dialog to let the user choose one string from a long
 * list. The basics:
 *
    String[] choices = {"A", "long", "array", "of", "strings"};
    ListDialog.initialize(componentInControllingFrame, choices,
                          "Dialog Title",
                          "A description of the list:");
    String selectedName = ListDialog.showDialog(locatorComponent,
                                                initialSelection);
 *
 */
public class ListDialog extends JDialog
{  private static ListDialog dialog;
   private static String value = "";
   private JList list;

   /** * Set up the dialog. The first argument can be null,
    * but it really should be a component in the dialog's
    * controlling frame. */
   public static void initialize(Component comp,
         String[] possibleValues, String title, String labelText)
   {  Frame frame = JOptionPane.getFrameForComponent(comp);
      dialog = new ListDialog(frame, possibleValues, title, labelText);
   }

   /** * Show the initialized dialog. The first argument should
    * be null if you want the dialog to come up in the center
    * of the screen. Otherwise, the argument should be the
    * component on top of which the dialog should appear.
    */
   public static String showDialog(Component comp, String initialValue)
   {  if (dialog != null)
      {  dialog.setValue(initialValue);
         dialog.setLocationRelativeTo(comp);
         dialog.setVisible(true);
      }
      else
      {  System.err.println("ListDialog requires you to call initialize "
         + "before calling showDialog.");
      }
      return value;
   }

   private void setValue(String newValue)
   {  value = newValue; list.setSelectedValue(value, true);
   }

   private ListDialog(Frame frame, Object[] data, String title,
         String labelText)
   {   super(frame, title, true);
       //buttons
       JButton cancelButton = new JButton("Cancel");
       final JButton setButton = new JButton("Open");
       cancelButton.addActionListener(new ActionListener()
       {  public void actionPerformed(ActionEvent e)
          { ListDialog.dialog.setVisible(false);
          }
       });
       setButton.addActionListener(new ActionListener()
       {  public void actionPerformed(ActionEvent e)
          { ListDialog.value = (String)(list.getSelectedValue());
            ListDialog.dialog.setVisible(false);
          }
       });
       getRootPane().setDefaultButton(setButton);
       //main part of the dialog
       list = new JList(data);
       list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
       list.addMouseListener(new MouseAdapter()
       {  public void mouseClicked(MouseEvent e)
          {  if (e.getClickCount() == 2)
             { setButton.doClick();
             }
          }
       });
       JScrollPane listScroller = new JScrollPane(list);
       listScroller.setPreferredSize(new Dimension(250, 80));
       //XXX: Must do the following, too, or else the scroller thinks
       //XXX: it's taller than it is:
       listScroller.setMinimumSize(new Dimension(250, 80));
       listScroller.setAlignmentX(LEFT_ALIGNMENT);
       //Create a container so that we can add a title around
       //the scroll pane. Can't add a title directly to the
       //scroll pane because its background would be white.
       //Lay out the label and scroll pane from top to button.
       JPanel listPane = new JPanel();
       listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
       JLabel label = new JLabel(labelText);
       label.setLabelFor(list);
       listPane.add(label);
       listPane.add(Box.createRigidArea(new Dimension(0,5)));
       listPane.add(listScroller);
       listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
       //Lay out the buttons from left to right.
       JPanel buttonPane = new JPanel();
       buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
       buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
       buttonPane.add(Box.createHorizontalGlue());
       buttonPane.add(setButton);
       buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
       buttonPane.add(cancelButton);
       //Put everything together, using the content pane's BorderLayout.
       Container contentPane = getContentPane();
       contentPane.add(listPane, BorderLayout.CENTER);
       contentPane.add(buttonPane, BorderLayout.SOUTH);
       pack();
    }

 }  // end class ListDialog


