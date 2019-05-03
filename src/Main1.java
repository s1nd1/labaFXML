import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main1 {
    private static ArrayList<String> menuElems = new ArrayList<>();

    static {
        menuElems.add("1. Валидировать");
        menuElems.add("2. Вывести");
        menuElems.add("3. Добавить");
        menuElems.add("4. Удалить");
        menuElems.add("5. Поиск");
        menuElems.add("0. Выйти");
    }

    public static void main(String[] args) {
        boolean stoped = false;

        DomHandler dh = new DomHandler();
        while(!stoped) {
            for (String elem : menuElems) System.out.println(elem);

            Scanner scanner = new Scanner(System.in);
            int point = scanner.nextInt();

            switch (point) {
                case 1:
                    System.out.println( "Данные валидны? " + validateXMLSchema("dbbibl.xsd", "dbbibl.xml"));
                    break;
                case 2:
                    dh.read();
                    break;
                case 3:
                    add();
                    break;
                case 4:
                    remove();
                    break;
                case 5:
                    search();
                    break;
                default:
                    stoped = true;
            }
        }
    }

    private static void search() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите аттрибут: ");
        String attribute = scanner.next();
        new SaxSearch().start(attribute);
    }

    private static void add() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите название: ");
        String name = scanner.next();

        System.out.print("Введите автора: ");
        String avtor = scanner.next();

        System.out.print("Введите год издания: ");
        String god = scanner.next();

        System.out.print("Введите название издания: ");
        String izdanie = scanner.next();

        new DomHandler().add(name,avtor, god, izdanie);
    }

    private static void remove() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите id: ");
        String id = scanner.next();

        new DomHandler().remove(id);
    }

    public static boolean validateXMLSchema(String xsdPath, String xmlPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }

    public static class DomHandler {

        public void read() {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse("dbbibl.xml");
                Node root = document.getDocumentElement();

                System.out.println("Список книг:");
                System.out.println();
                NodeList db = root.getChildNodes();
                NodeList knigi = db.item(0).getChildNodes();
                for (int i = 0; i < knigi.getLength(); i++) {
                    Node kniga = knigi.item(i);
                    System.out.println("id: " + kniga.getAttributes().getNamedItem("id").getNodeValue());
                    if (kniga.getNodeType() != Node.TEXT_NODE) {
                        NodeList knigaProps = kniga.getChildNodes();
                        for(int j = 0; j < knigaProps.getLength(); j++) {
                            Node knigaProp = knigaProps.item(j);
                            if (knigaProp.getNodeType() != Node.TEXT_NODE) {
                                System.out.println(knigaProp.getNodeName() + ":" + knigaProp.getChildNodes().item(0).getTextContent());
                            }
                        }
                        System.out.println();
                    }
                }

            } catch (ParserConfigurationException ex) {
                ex.printStackTrace(System.out);
            } catch (SAXException ex) {
                ex.printStackTrace(System.out);
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }

        public void add(String nm, String av, String gd, String izd) {
            try {

                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse("dbbibl.xml");

                Node root = document.getDocumentElement();
                Node knigi = root.getChildNodes().item(0);
                NodeList es = knigi.getChildNodes();
                Node e = es.item(es.getLength()-1);

                String lastId = e.getAttributes().getNamedItem("id").getNodeValue();

                Element kniga = document.createElement("Knigaspisok");
                Integer newId = Integer.parseInt(lastId) + 1;
                kniga.setAttribute("id", "" + newId);

                Element name = document.createElement("Name");
                name.setTextContent(nm);

                Element avtor = document.createElement("Avtor");
                avtor.setTextContent(av);

                Element god = document.createElement("God");
                god.setTextContent(gd);

                Element izdanie = document.createElement("Izdanie");
                izdanie.setTextContent(izd);

                kniga.appendChild(name);
                kniga.appendChild(avtor);
                kniga.appendChild(god);
                kniga.appendChild(izdanie);
                knigi.appendChild(kniga);

                writeDocument(document);
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                ex.printStackTrace();
            }
        }

        public void remove(String id) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse("dbbibl.xml");

                Node root = document.getDocumentElement();
                Node empls = root.getChildNodes().item(0);
                NodeList emps = empls.getChildNodes();
                for (int i=0; i < emps.getLength(); i++) {
                    if(id.equals(emps.item(i).getAttributes().getNamedItem("id").getNodeValue())) empls.removeChild(emps.item(i));
                }

                writeDocument(document);
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                ex.printStackTrace();
            }
        }

        private static void writeDocument(Document document) throws TransformerFactoryConfigurationError {
            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(document);
                FileOutputStream fos = new FileOutputStream("dbbibl.xml");
                StreamResult result = new StreamResult(fos);
                tr.transform(source, result);
            } catch (TransformerException | IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    public static class SaxSearch {
        public void start(String tag) {
            final String fileName = "dbbibl.xml";

            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                DefaultHandler handler = new DefaultHandler() {
                    boolean name = false;

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        if (qName.equalsIgnoreCase(tag)) {
                            name = true;
                        }
                    }

                    @Override
                    public void characters(char ch[], int start, int length) throws SAXException {
                        if (name) {
                            System.out.println(tag + ": " + new String(ch, start, length));
                            name = false;
                        }
                    }
                };

                saxParser.parse(fileName, handler);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
