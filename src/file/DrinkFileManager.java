package file;
import datastructure.DrinkLinkedList;
import exception.FileDataException;
import model.Drink;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
public class DrinkFileManager extends FileManager{
    private final Path path=dataDir.resolve("drinks.txt");
    public DrinkLinkedList load() throws FileDataException { ensureDefault(); DrinkLinkedList list=new DrinkLinkedList(); for(String s:readAll(path)) if(!s.isBlank()) list.add(Drink.fromCsv(s)); return list; }
    public void save(DrinkLinkedList drinks) throws FileDataException { List<String> lines=new ArrayList<>(); for(Drink d:drinks.toList()) lines.add(d.toCsv()); writeAll(path,lines); }
    private void ensureDefault() throws FileDataException { if(!java.nio.file.Files.exists(path)){ List<String> d=List.of("1,믹스커피,200,10,0","2,고급믹스커피,300,10,0","3,물,450,10,0","4,캔커피,500,10,0","5,이온음료,550,10,0","6,고급캔커피,700,10,0","7,탄산음료,750,10,0","8,특화음료,800,10,0"); writeAll(path,d);} }
}
