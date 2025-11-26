package Application.Views.InstructorViews;

import Application.Components.StyleConstants;
import Application.Components.StyledButton;
import Domain.Concretes.Instructor;
import Domain.Concretes.Section;
import Domain.Database.sqliteConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseStatisticsView extends JPanel {

    private final Instructor instructor;
    private JPanel listContainer;

    public CourseStatisticsView(Instructor instructor) {
        this.instructor = instructor;

        setLayout(new BorderLayout());
        setBackground(StyleConstants.TERTIARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Course Analytics & Statistics");
        title.setFont(StyleConstants.HEADER_FONT);
        title.setForeground(StyleConstants.WHITE);
        header.add(title);

        StyledButton refreshBtn = new StyledButton("Refresh Data", StyleConstants.SECONDARY_COLOR);
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.addActionListener(e -> loadStats());
        header.add(Box.createHorizontalStrut(20));
        header.add(refreshBtn);

        // --- Content Scroll Area ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(StyleConstants.DIM_WHITE);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(StyleConstants.DIM_WHITE);
        scroll.getViewport().setBackground(StyleConstants.DIM_WHITE);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadStats();
    }

    private void loadStats() {
        listContainer.removeAll();

        List<String> mySections = getAssignedSections();

        if (mySections.isEmpty()) {
            JLabel empty = new JLabel("No active sections found for analytics.");
            empty.setFont(StyleConstants.NORMAL_FONT);
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(Box.createVerticalStrut(50));
            listContainer.add(empty);
        } else {
            for (String secId : mySections) {
                try {
                    Section section = new Section(secId);
                    StatsPanel panel = new StatsPanel(section);
                    listContainer.add(panel);
                    listContainer.add(Box.createVerticalStrut(15));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private List<String> getAssignedSections() {
        try {
            List<String> sections = new ArrayList<>();
            String sql = "SELECT section_id FROM teaching WHERE instructor_id = ?";
            try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, instructor.getId());
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) sections.add(rs.getString("section_id"));
            }
            return sections;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // --- Inner Class for Section Statistics ---
    private class StatsPanel extends JPanel {
        private final Section section;
        private final JPanel contentPanel;
        private boolean isExpanded = false;
        private final StyledButton toggleBtn;
        private final List<Float> totalScores;

        public StatsPanel(Section section) {
            this.section = section;
            this.totalScores = fetchTotalScores();

            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
                    new EmptyBorder(15, 20, 15, 20)
            ));

            // Top Bar
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setOpaque(false);

            String titleText = "<html><b style='font-size:16px; color:#2c3e50'>" + section.getId() + "</b>" +
                    " <span style='color:#7f8c8d'> | " + section.getSemester() + "</span>" +
                    " <span style='color:#95a5a6; font-size:11px'> (" + totalScores.size() + " Students)</span></html>";
            JLabel title = new JLabel(titleText);

            toggleBtn = new StyledButton("View Analytics", StyleConstants.PRIMARY_COLOR);
            toggleBtn.setPreferredSize(new Dimension(140, 35));
            toggleBtn.addActionListener(e -> toggleExpansion());

            topBar.add(title, BorderLayout.WEST);
            topBar.add(toggleBtn, BorderLayout.EAST);

            // Content Panel
            contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setVisible(false);
            contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

            add(topBar, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void toggleExpansion() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                toggleBtn.setText("Hide Analytics");
                buildAnalyticsUI();
            } else {
                toggleBtn.setText("View Analytics");
                contentPanel.removeAll();
            }
            contentPanel.setVisible(isExpanded);
            revalidate();
            repaint();
        }

        private void buildAnalyticsUI() {
            contentPanel.removeAll();

            if (totalScores.isEmpty()) {
                JLabel empty = new JLabel("Not enough data to generate statistics.");
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(empty);
                return;
            }

            // 1. Key Metrics Cards
            JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
            metricsPanel.setOpaque(false);
            metricsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            float max = Collections.max(totalScores);
            float min = Collections.min(totalScores);
            float avg = (float) totalScores.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
            float median = calculateMedian(totalScores);

            metricsPanel.add(createMetricCard("Average", String.format("%.1f", avg), new Color(52, 152, 219)));
            metricsPanel.add(createMetricCard("Median", String.format("%.1f", median), new Color(155, 89, 182)));
            metricsPanel.add(createMetricCard("Highest", String.format("%.1f", max), new Color(46, 204, 113)));
            metricsPanel.add(createMetricCard("Lowest", String.format("%.1f", min), new Color(231, 76, 60)));

            contentPanel.add(metricsPanel);
            contentPanel.add(Box.createVerticalStrut(25));

            // 2. Grade Distribution
            JLabel gradeTitle = new JLabel("Grade Distribution");
            gradeTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gradeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(gradeTitle);
            contentPanel.add(Box.createVerticalStrut(10));

            JPanel gradesPanel = createGradeDistributionPanel();
            gradesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(gradesPanel);
            contentPanel.add(Box.createVerticalStrut(25));

            // 3. Score Chart
            JLabel chartTitle = new JLabel("Score Distribution Histogram");
            chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            chartTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(chartTitle);
            contentPanel.add(Box.createVerticalStrut(10));

            // Pass avg and median to graph
            ScoreDistributionGraph graph = new ScoreDistributionGraph(totalScores, avg, median);
            graph.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(graph);
        }

        private JPanel createMetricCard(String label, String value, Color accent) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(new Color(250, 250, 250));
            card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, accent));

            JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
            valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
            valLbl.setForeground(Color.DARK_GRAY);

            JLabel titleLbl = new JLabel(label, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLbl.setForeground(Color.GRAY);
            titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));

            card.add(valLbl, BorderLayout.CENTER);
            card.add(titleLbl, BorderLayout.SOUTH);
            return card;
        }

        private JPanel createGradeDistributionPanel() {
            Map<String, Integer> counts = calculateGradeCounts();
            JPanel p = new JPanel(new GridLayout(1, 9, 5, 0));
            p.setOpaque(false);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            String[] grades = {"O", "A", "A-", "B", "B-", "C", "C-", "D", "F"};
            Color[] colors = {
                    new Color(39, 174, 96), new Color(46, 204, 113), new Color(88, 214, 141),
                    new Color(52, 152, 219), new Color(93, 173, 226),
                    new Color(241, 196, 15), new Color(243, 156, 18),
                    new Color(230, 126, 34), new Color(231, 76, 60)
            };

            for (int i=0; i<grades.length; i++) {
                String g = grades[i];
                int count = counts.getOrDefault(g, 0);

                JPanel box = new JPanel(new BorderLayout());
                box.setBackground(colors[i]);
                box.setBorder(new EmptyBorder(5, 5, 5, 5));

                JLabel gLbl = new JLabel(g, SwingConstants.CENTER);
                gLbl.setForeground(Color.WHITE);
                gLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

                JLabel cLbl = new JLabel(String.valueOf(count), SwingConstants.CENTER);
                cLbl.setForeground(new Color(255, 255, 255, 220));
                cLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                box.add(gLbl, BorderLayout.CENTER);
                box.add(cLbl, BorderLayout.SOUTH);
                p.add(box);
            }
            return p;
        }

        private Map<String, Integer> calculateGradeCounts() {
            Map<String, Integer> map = new HashMap<>();
            for (float s : totalScores) {
                String g;
                if (s >= section.getO()) g = "O";
                else if (s >= section.getA()) g = "A";
                else if (s >= section.getA_()) g = "A-";
                else if (s >= section.getB()) g = "B";
                else if (s >= section.getB_()) g = "B-";
                else if (s >= section.getC()) g = "C";
                else if (s >= section.getC_()) g = "C-";
                else if (s >= section.getD()) g = "D";
                else g = "F";
                map.put(g, map.getOrDefault(g, 0) + 1);
            }
            return map;
        }

        private float calculateMedian(List<Float> scores) {
            List<Float> sorted = new ArrayList<>(scores);
            Collections.sort(sorted);
            int size = sorted.size();
            if (size == 0) return 0;
            if (size % 2 == 1) {
                return sorted.get(size / 2);
            } else {
                return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0f;
            }
        }

        private List<Float> fetchTotalScores() {
            List<Float> list = new ArrayList<>();
            String sql = "SELECT labs, quiz, mid, end, assign, proj, bonus FROM records WHERE section_id = ?";
            try (Connection conn = sqliteConnector.connect("jdbc:sqlite:erp.db");
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, section.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    float total = rs.getFloat("labs") + rs.getFloat("quiz") +
                            rs.getFloat("mid") + rs.getFloat("end") +
                            rs.getFloat("assign") + rs.getFloat("proj") +
                            rs.getFloat("bonus");
                    list.add(total);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }
    }

    // --- Custom Histogram Component ---
    private static class ScoreDistributionGraph extends JPanel {
        private final List<Float> data;
        private final int[] bins;
        private final int binSize = 10;
        private final int numBins = 11; // 0-9, 10-19, ..., 90-99, 100+
        private final float average;
        private final float median;

        public ScoreDistributionGraph(List<Float> data, float average, float median) {
            this.data = data;
            this.average = average;
            this.median = median;
            this.bins = new int[numBins];
            calculateBins();

            setPreferredSize(new Dimension(0, 250));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        }

        private void calculateBins() {
            for (float score : data) {
                int index = (int) (score / binSize);
                if (index >= numBins) index = numBins - 1; // 100+ goes to last bin
                if (index < 0) index = 0;
                bins[index]++;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padding = 40;
            int bottomPad = 40;
            int leftPad = 50;

            int graphW = w - padding - leftPad;
            int graphH = h - padding - bottomPad;

            // Draw Axes
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(leftPad, h - bottomPad, w - padding, h - bottomPad); // X-Axis
            g2.drawLine(leftPad, padding, leftPad, h - bottomPad); // Y-Axis

            // Labels
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));

            // Y-Axis Label
            AffineTransform orig = g2.getTransform();
            g2.rotate(-Math.PI / 2);
            g2.drawString("Student Count", -h/2 - 40, 20);
            g2.setTransform(orig);

            // X-Axis Label
            g2.drawString("Score Range", w/2 - 30, h - 10);

            // Calculate Max Y for scaling
            int maxCount = 0;
            for (int count : bins) maxCount = Math.max(maxCount, count);
            if (maxCount == 0) maxCount = 1; // Prevent div/0

            // Draw Bars
            int barWidth = graphW / numBins;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            for (int i = 0; i < numBins; i++) {
                int count = bins[i];
                int barHeight = (int) (((double) count / maxCount) * (graphH - 20)); // -20 for top headroom

                int x = leftPad + (i * barWidth);
                int y = h - bottomPad - barHeight;

                // Bar
                if (count > 0) {
                    g2.setColor(new Color(100, 149, 237));
                    g2.fill(new Rectangle2D.Float(x + 2, y, barWidth - 4, barHeight));

                    // Count Label Top
                    g2.setColor(Color.BLACK);
                    String countStr = String.valueOf(count);
                    int strW = g2.getFontMetrics().stringWidth(countStr);
                    g2.drawString(countStr, x + (barWidth - strW) / 2, y - 5);
                }

                // X-Axis Tick Label
                g2.setColor(Color.GRAY);
                String range;
                if (i == numBins - 1) range = "100+";
                else range = String.valueOf(i * binSize);

                int rangeW = g2.getFontMetrics().stringWidth(range);
                g2.drawString(range, x + (barWidth - rangeW) / 2, h - bottomPad + 15);
            }

            // Y-Axis Ticks
            for (int i = 0; i <= maxCount; i++) {
                int y = h - bottomPad - (int)(((double)i / maxCount) * (graphH - 20));
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(leftPad, y, w - padding, y); // Grid line

                g2.setColor(Color.GRAY);
                g2.drawString(String.valueOf(i), leftPad - 20, y + 5);
            }

            // --- Draw Average & Median Lines ---

            // 1. Calculate X Positions
            float maxAxisVal = numBins * 10.0f; // 110

            int xAvg = leftPad + (int) ((average / maxAxisVal) * graphW);
            if (xAvg > leftPad + graphW) xAvg = leftPad + graphW;

            int xMed = leftPad + (int) ((median / maxAxisVal) * graphW);
            if (xMed > leftPad + graphW) xMed = leftPad + graphW;

            // 2. Draw Lines (without labels attached)
            drawDottedLine(g2, xAvg, Color.MAGENTA, h, bottomPad);
            drawDottedLine(g2, xMed, new Color(255, 140, 0), h, bottomPad);

            // 3. Draw Legend in Top Right
            drawLegend(g2, w - padding, padding);
        }

        private void drawDottedLine(Graphics2D g2, int x, Color color, int h, int bottomPad) {
            Stroke originalStroke = g2.getStroke();
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2.drawLine(x, h - bottomPad, x, 40);
            g2.setStroke(originalStroke);
        }

        private void drawLegend(Graphics2D g2, int rightX, int topY) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();

            String avgText = "Avg: " + String.format("%.1f", average);
            String medText = "Med: " + String.format("%.1f", median);

            int boxW = Math.max(fm.stringWidth(avgText), fm.stringWidth(medText)) + 40;
            int boxH = 50;
            int x = rightX - boxW;
            int y = topY;

            // Background for legend
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRect(x, y, boxW, boxH);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRect(x, y, boxW, boxH);

            // Draw Avg Key
            g2.setColor(Color.MAGENTA);
            g2.fillRect(x + 10, y + 15, 10, 10); // Color square
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(avgText, x + 25, y + 25);

            // Draw Med Key
            g2.setColor(new Color(255, 140, 0));
            g2.fillRect(x + 10, y + 35, 10, 10); // Color square
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(medText, x + 25, y + 45);
        }
    }
}