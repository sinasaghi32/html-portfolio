package manager;
import exception.*;
import file.AdminFileManager;
public class AdminManager{ private final AdminFileManager file=new AdminFileManager(); private String password; public AdminManager() throws FileDataException{ password=file.loadPassword(); }
 public void login(String p) throws InvalidPasswordException{ if(!password.equals(p)) throw new InvalidPasswordException("관리자 비밀번호가 틀렸습니다."); }
 public void changePassword(String p) throws InvalidPasswordException, FileDataException{ validate(p); password=p; file.savePassword(p); }
 public static void validate(String p) throws InvalidPasswordException{ if(p==null||p.length()<8||!p.matches(".*[0-9].*")||!p.matches(".*[^a-zA-Z0-9].*")) throw new InvalidPasswordException("비밀번호는 8자 이상, 숫자 1개 이상, 특수문자 1개 이상이어야 합니다."); }
}
