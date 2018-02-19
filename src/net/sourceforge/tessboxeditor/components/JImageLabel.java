/**
 * Copyright @ 20011 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.tessboxeditor.components;

import net.sourceforge.tessboxeditor.Gui;
import net.sourceforge.tessboxeditor.datamodel.TessBox;
import net.sourceforge.tessboxeditor.datamodel.TessBoxCollection;
import net.sourceforge.vietocr.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class JImageLabel extends JLabel {

  private Gui gui;
  private TessBoxCollection boxes;
  private JTable table;
  private boolean boxClickAction;

  /**
   * Creates a new instance of JImageLabel
   */
  public JImageLabel() {
    MouseAdapter mouseAdapter = new MouseAdapter();
    addMouseListener(mouseAdapter);
    addMouseMotionListener(mouseAdapter);
  }

  private class MouseAdapter implements MouseListener, MouseMotionListener {
    private static final int MARGIN = 8;
    private static final int I = 3;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private Rectangle pressedRect;
    private MouseState pressedState;

    @Override
    public void mousePressed(MouseEvent e) {
      if (boxes == null) {
        return;
      }

      TessBox box = boxes.hitObject(e.getPoint());
      if (box == null) {
        if (!e.isControlDown()) {
          boxes.deselectAll();
          repaint();
          table.clearSelection();
        }
        pressedRect = null;
      } else {
        if (!e.isControlDown()) {
          boxes.deselectAll();
          table.clearSelection();
        }
        pressedRect = box.getRect();
        pressedState = getMouseState(e);
        box.setSelected(!box.isSelected()); // toggle selection
        repaint();
        // select corresponding table rows
        boxClickAction = true;
        java.util.List<TessBox> boxesOfCurPage = boxes.toList(); // boxes of current page
        for (TessBox selectedBoxes : boxes.getSelectedBoxes()) {
          int index = boxesOfCurPage.indexOf(selectedBoxes);
          table.addRowSelectionInterval(index, index);
          Rectangle rect = table.getCellRect(index, 0, true);
          table.scrollRectToVisible(rect);
        }
        boxClickAction = false;
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      gui.updateXYWHData();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (canDrag(e)) {
        if (MouseState.DEFAULT.equals(pressedState)) drag(e);
        else resize(e);

//        gui.updateXYWHData();
        repaint();
      }
    }

    private boolean canDrag(MouseEvent e) {
      return pressedRect != null && e.getX() >= 0 && e.getY() >= 0;
    }

    private void drag(MouseEvent e) {
      pressedRect.setLocation(e.getPoint());
    }

    private void resize(MouseEvent e) {
      switch (pressedState) {
        case DEFAULT:
          drag(e);
          break;
        case NW_RESIZE:
          resizeLeftSide(e);
          resizeTopSide(e);
          break;
        case SW_RESIZE:
          resizeLeftSide(e);
          resizeBottomSide(e);
          break;
        case NE_RESIZE:
          resizeTopSide(e);
          resizeRightSide(e);
          break;
        case SE_RESIZE:
          resizeRightSide(e);
          resizeBottomSide(e);
          break;
        case E_RESIZE:
          resizeRightSide(e);
          break;
        case W_RESIZE:
          resizeLeftSide(e);
          break;
        case N_RESIZE:
          resizeTopSide(e);
          break;
        case S_RESIZE:
          resizeBottomSide(e);
          break;
        default:
          throw new IllegalStateException("Unknown state: " + pressedState.name());
      }
    }

    private void resizeLeftSide(MouseEvent e) {
      if (e.getX() >= pressedRect.x) increaseLeftSide();
      else decreaseLeftSide();
    }

    private void resizeTopSide(MouseEvent e) {
      if (e.getY() >= pressedRect.y) increaseTopSide();
      else decreaseTopSide();
    }

    private void resizeRightSide(MouseEvent e) {
      if (e.getX() >= pressedRect.x + pressedRect.width) increaseRightSide();
      else decreaseRightSide();
    }

    private void resizeBottomSide(MouseEvent e) {
      if (e.getY() >= pressedRect.y + pressedRect.height) increaseBottomSide();
      else decreaseBottomSide();
    }

    private void increaseLeftSide() {
      pressedRect.setLocation(pressedRect.x + I, pressedRect.y);
      pressedRect.setSize(pressedRect.width - I, pressedRect.height);
    }

    private void decreaseLeftSide() {
      pressedRect.setLocation(pressedRect.x - I, pressedRect.y);
      pressedRect.setSize(pressedRect.width + I, pressedRect.height);
    }

    private void increaseTopSide() {
      pressedRect.setLocation(pressedRect.x, pressedRect.y + I);
      pressedRect.setSize(pressedRect.width, pressedRect.height - I);
    }

    private void decreaseTopSide() {
      pressedRect.setLocation(pressedRect.x, pressedRect.y - I);
      pressedRect.setSize(pressedRect.width, pressedRect.height + I);
    }

    private void increaseRightSide() {
      pressedRect.setSize(pressedRect.width + I, pressedRect.height);
    }

    private void decreaseRightSide() {
      pressedRect.setSize(pressedRect.width - I, pressedRect.height);
    }

    private void increaseBottomSide() {
      pressedRect.setSize(pressedRect.width, pressedRect.height + I);
    }

    private void decreaseBottomSide() {
      pressedRect.setSize(pressedRect.width, pressedRect.height - I);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      e.getComponent().setCursor(canResize(e) ? getMouseState(e).getCursor() : Cursor.getDefaultCursor());
    }

    private boolean canResize(MouseEvent e) {
      return pressedRect != null && pressedRect.contains(e.getPoint());
    }

    private MouseState getMouseState(MouseEvent e) {
      boolean left = isLeftSide(e), top = isTopSide(e), right = isRightSide(e), bottom = isBottomSide(e);

      if (left && top) return MouseState.NW_RESIZE;
      else if (left && bottom) return MouseState.SW_RESIZE;
      else if (right && top) return MouseState.NE_RESIZE;
      else if (right && bottom) return MouseState.SE_RESIZE;
      else if (right) return MouseState.E_RESIZE;
      else if (left) return MouseState.W_RESIZE;
      else if (top) return MouseState.N_RESIZE;
      else if (bottom) return MouseState.S_RESIZE;
      else return MouseState.DEFAULT;
    }

    private boolean isLeftSide(MouseEvent e) {
      return e.getY() >= pressedRect.y && e.getY() <= pressedRect.y + pressedRect.height
          && e.getX() + MARGIN >= pressedRect.x && e.getX() - MARGIN <= pressedRect.x;
    }

    private boolean isTopSide(MouseEvent e) {
      return e.getX() >= pressedRect.x && e.getX() <= pressedRect.x + pressedRect.width
          && e.getY() + MARGIN >= pressedRect.y && e.getY() - MARGIN <= pressedRect.y;
    }

    private boolean isRightSide(MouseEvent e) {
      int x = pressedRect.x + pressedRect.width;
      return e.getY() >= pressedRect.y && e.getY() <= pressedRect.y + pressedRect.height
          && e.getX() + MARGIN >= x && e.getX() - MARGIN <= x;
    }

    private boolean isBottomSide(MouseEvent e) {
      int y = pressedRect.y + pressedRect.height;
      return e.getX() >= pressedRect.x && e.getX() <= pressedRect.x + pressedRect.width
          && e.getY() + MARGIN >= y && e.getY() - MARGIN <= y;
    }
  }

  private enum MouseState {
    DEFAULT {
      @Override
      Cursor getCursor() {
        return Cursor.getDefaultCursor();
      }
    }, NW_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
      }
    }, SW_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
      }
    }, NE_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
      }
    }, SE_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
      }
    }, E_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
      }
    }, W_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
      }
    }, N_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
      }
    }, S_RESIZE {
      @Override
      Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
      }
    };

    Cursor getCursor() {
      throw new AbstractMethodError();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    // automatically called when repaint
    super.paintComponent(g);

    if (boxes == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(Color.GREEN);
    boolean resetColor = false;
//        int height = getHeight();

    for (TessBox box : boxes.toList()) {
      if (box.isSelected()) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.RED);
        resetColor = true;
      }
      Rectangle rect = box.getRect();
      g2d.draw(rect);
//            g2d.drawRect(rect.x, height - rect.y - rect.height, rect.width, rect.height);
      if (resetColor) {
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.GREEN);
        resetColor = false;
      }
    }
  }

  @Override
  public boolean contains(int x, int y) {
    if (this.boxes != null) {
      TessBox curBox = this.boxes.hitObject(new Point(x, y));
      if (curBox != null) {
        String curChrs = curBox.getChrs();
        setToolTipText(String.format("<html><h1><font face=\"%s\" >%s</font> : %s</h1></html>", this.getFont().getName(), curChrs, Utils.toHex(curChrs)));
      } else {
        setToolTipText(null);
      }
    }
    return super.contains(x, y);
  }

  public void setGui(Gui gui) {
    this.gui = gui;
  }

  public void setBoxes(TessBoxCollection boxes) {
    this.boxes = boxes;
    repaint();
  }

  /**
   * @param table the table to set
   */
  public void setTable(JTable table) {
    this.table = table;
  }

  /**
   * @return the boxClickAction
   */
  public boolean isBoxClickAction() {
    return boxClickAction;
  }
}
