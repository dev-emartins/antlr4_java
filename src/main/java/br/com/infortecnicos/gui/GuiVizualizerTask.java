package br.com.infortecnicos.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

public class GuiVizualizerTask implements Runnable {
    private final Parser parser;
    private final ParseTree tree;
    private double zoomFactor = 1.5;

    public GuiVizualizerTask(Parser parser, ParseTree tree){
        this.parser = parser;
        this.tree = tree;
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ANTLR4 Parse Tree");
            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setScale(zoomFactor);
            JPanel canvas = new JPanel(new GridBagLayout());
            canvas.setBackground(new Color(240, 240, 240));
            canvas.setPreferredSize(new Dimension(5000, 5000));
            canvas.add(viewer);

            JScrollPane scrollPane = new JScrollPane(canvas);
            scrollPane.setBorder(null);
            SwingUtilities.invokeLater(() -> {
                JViewport vp = scrollPane.getViewport();
                vp.setViewPosition(new Point(2500 - vp.getWidth()/2, 2500 - vp.getHeight()/2));
            });

            setupInteractivity(viewer, canvas, scrollPane);

            frame.add(scrollPane);
            frame.setSize(1200, 900);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        System.out.println("Interface aberta (verbose = true).");
        System.out.println("Pressione ENTER para encerrar.");
        try {
            System.in.read();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void setupInteractivity(TreeViewer viewer, JPanel canvas, JScrollPane scrollPane) {
        JViewport viewport = scrollPane.getViewport();
        Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        Cursor grabCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

        canvas.setCursor(handCursor);

        MouseAdapter interactiveAdapter = new MouseAdapter() {
            private Point origin;

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.1 * e.getWheelRotation();
                zoomFactor = Math.max(0.1, Math.min(zoomFactor - delta, 5.0));
                viewer.setScale(zoomFactor);
                canvas.revalidate();
                canvas.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                canvas.setCursor(grabCursor);
                origin = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                canvas.setCursor(handCursor);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JComponent view = (JComponent) viewport.getView();
                    Point vPos = viewport.getViewPosition();
                    int dx = origin.x - e.getX();
                    int dy = origin.y - e.getY();
                    vPos.translate(dx, dy);
                    vPos.x = Math.max(0, Math.min(vPos.x, view.getWidth() - viewport.getWidth()));
                    vPos.y = Math.max(0, Math.min(vPos.y, view.getHeight() - viewport.getHeight()));

                    viewport.setViewPosition(vPos);
                }
            }
        };
        canvas.addMouseWheelListener(interactiveAdapter);
        canvas.addMouseListener(interactiveAdapter);
        canvas.addMouseMotionListener(interactiveAdapter);
        viewer.addMouseWheelListener(interactiveAdapter);
        viewer.addMouseListener(interactiveAdapter);
        viewer.addMouseMotionListener(interactiveAdapter);
    }
}