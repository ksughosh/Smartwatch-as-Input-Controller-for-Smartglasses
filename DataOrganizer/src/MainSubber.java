import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by SughoshKumar on 25/01/16.
 */
public class MainSubber {
    private ArrayList<File> fileList;
    private File XMLFolder;
    private ArrayList<XMLHolder> SourceDestinationData;
    private final String fname = "Collection.csv";
    private final String mt = "movement.csv";
    private float radiusOfTargets, indexOfDifficulty, distanceBetweenTargets;
    private final int pointSize = 8;
    ArrayList<Double> top = new ArrayList<>();
    ArrayList<Double> bottom = new ArrayList<>();
    ArrayList<Double> left = new ArrayList<>();
    ArrayList<Double> right = new ArrayList<>();

    public MainSubber(String arg){
        XMLFolder = new File(arg);
        SourceDestinationData = new ArrayList<>();
        fileList = new ArrayList<>();
        writeHeader();
        readFile();
    }

    private void readFile(){
        if (XMLFolder.isDirectory()){
            for (File file : XMLFolder.listFiles()){
                if (file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).equals("xml")){
                    fileList.add(file);
                }
            }
        }
        fileList.sort((o1, o2) -> {
            String[] s1 = o1.getName().split("_");
            String[] s2 = o2.getName().split("_");
            Float partA1 = Float.parseFloat(s1[1]);
            Float partB1 = Float.parseFloat(s2[1]);
            Float partA2 = Float.parseFloat(s1[2].replace(".xml", ""));
            Float partB2 = Float.parseFloat(s2[2].replace(".xml", ""));
            if (partA1.equals(partB1))
                return partA2.compareTo(partB2);
            else
                return partB1.compareTo(partA1);
        });
        for (File xmlFile : fileList)
            parseBasicXML(xmlFile);
        System.out.println("Collection Files Generated Successfully!");
    }



    private void parseXMLDataForPathPatternsAndDraw(File XMLFile) {
        try {
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBF.newDocumentBuilder();
            Document document = docBuilder.parse(XMLFile);
            document.getDocumentElement().normalize();
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("TargetBlock");
            Coordinates source, destination;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    String sxValue = e.getAttribute("SourceX");
                    String syValue = e.getAttribute("SourceY");
                    String sAngle = e.getAttribute("SourceAngle");
                    String dxValue = e.getAttribute("TargetX");
                    String dyValue = e.getAttribute("TargetY");
                    String dAngle = e.getAttribute("TargetAngle");
                    String ID = e.getAttribute("index");
                    if (Integer.parseInt(ID) != 1) {
                        source = new Coordinates(Float.parseFloat(sxValue), Float.parseFloat(syValue), Float.parseFloat(sAngle));
                        destination = new Coordinates(Float.parseFloat(dxValue), Float.parseFloat(dyValue), Float.parseFloat(dAngle));
                        XMLHolder currentData = new XMLHolder(source, destination);
                        NodeList mNodes = node.getChildNodes();
                        for (int j = 0; j < mNodes.getLength(); j++) {
                            Node innerNode = mNodes.item(j);
                            if (innerNode.getNodeType() == Node.ELEMENT_NODE && innerNode.getNodeName().equalsIgnoreCase("pointerposition")) {
                                Element innerElement = (Element) innerNode;
                                float relativeTime = Float.parseFloat(innerElement.getAttribute("RelativeTime"));
                                float startTime = Float.parseFloat(innerElement.getAttribute("StartTime"));
                                float x = Float.parseFloat(innerElement.getAttribute("x"));
                                float y = Float.parseFloat(innerElement.getAttribute("y"));
                                currentData.add(x, y, relativeTime, startTime);
                            }

                        }
                        SourceDestinationData.add(currentData);
                    }
                }
            }
            writeToFile();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeToFile() {
        File f = new File(XMLFolder, fname);
        try {
            FileOutputStream fs = new FileOutputStream(f, true);
            OutputStreamWriter os = new OutputStreamWriter(fs);
            for (XMLHolder d1 : SourceDestinationData) {
                DataHolder d = d1.getHolder().get(0);
                DataHolder dt = d1.getHolder().get(d1.getHolder().size()-1);
                double sum = 0;
                double distance;
                double meanAcceleration = 0;
                double meanVelocity = 0;
                double v, a, t;
                double u = 0;
                for (int i = 0; i < d1.getHolder().size()-1; i++){
                    distance = Math.sqrt(Math.pow((d1.getHolder().get(i+1).getX() - d1.getHolder().get(i).getX()),2) +
                            Math.pow((d1.getHolder().get(i+1).getY() - d1.getHolder().get(i).getY()),2));
                    t = d1.getHolder().get(i+1).getRelativeTime();
                    v = distance/t;
                    meanVelocity += v;
                    a = (v - u)/t;
                    u = v;
                    meanAcceleration += a;
                    sum += distance;
                }
                writeOvershoot(d1);
                meanVelocity = meanVelocity/d1.getHolder().size();
                meanAcceleration = meanAcceleration/d1.getHolder().size();
                os.write(d.getX() + ", " + d.getY() + ", " + dt.getX() +", " +dt.getY() +", "+ sum +", " +meanVelocity + ", "+ meanAcceleration);
                os.write("\n");
                os.flush();
            }
            os.close();
            fs.close();
            SourceDestinationData.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeOvershoot(XMLHolder data) throws IOException {
        File overshoot = new File(XMLFolder, "overshoot.csv");
        int index = 0;
        String oversh = "No";
        double time = 0;
        for (DataHolder d : data.getHolder()){
            if (!isInClickArea(d, data.getDestination())&& data.getHolder().indexOf(d) > index && index > 0){
                oversh = "Yes";
                time += d.getRelativeTime();
            }
            else if (isInClickArea(d, data.getDestination()) && index == 0){
                index = data.getHolder().indexOf(d);
            }
            else if (isInClickArea(d, data.getSource()));

        }
        FileOutputStream fos = new FileOutputStream(overshoot, true);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(String.valueOf(time));
        osw.write("\n");
        osw.flush();
        osw.close();
        fos.close();
    }

    private void parseBasicXML(File xmlFile){
        try {
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBF.newDocumentBuilder();
            Document document = docBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // get the root node of the numbers
            NodeList nodeList = document.getElementsByTagName("BLOCK");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    indexOfDifficulty = Float.parseFloat(e.getAttribute("ID"));
                    distanceBetweenTargets = Integer.parseInt(e.getAttribute("Distance"));
                }
            }
            computeRadius();
            parseXMLDataForPathPatternsAndDraw(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }

    private void computeRadius(){
        radiusOfTargets = (float) (distanceBetweenTargets/(Math.pow(2,indexOfDifficulty) -1))/2;
    }


    private boolean isInClickArea(DataHolder c, Coordinates d){
        float threshold = 0.4f * radiusOfTargets;
        float x = d.getX() - c.getX();
        float y = d.getY() - c.getY();
        double distance = Math.sqrt(x*x + y*y);
        return (distance < (radiusOfTargets - threshold + pointSize));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeHeader(){
        File f = new File(XMLFolder, fname);
        File f1 = new File(XMLFolder, mt);
        File overshoot  = new File(XMLFolder, "overshoot.csv");
        try {
            if (f.exists())
                f.delete();

            if (f1.exists())
                f1.delete();

            if (overshoot.exists()){
                overshoot.delete();
                overshoot.createNewFile();
            }
            else
                overshoot.createNewFile();

            FileOutputStream fs = new FileOutputStream(f, true);
            OutputStreamWriter os = new OutputStreamWriter(fs);
            os.write("StartX, StartY, EndX, EndY, TravelDistance, Mean");
            os.write("\n");
            os.flush();
            os.close();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
