import java.io.Serializable;
import java.time.LocalDateTime;

public class Pacchetto implements Serializable {
	private String mess;
	private int code;
	private LocalDateTime date;

	public Pacchetto() {
		super();
		this.date = LocalDateTime.now();
	}

	public Pacchetto(String mess, int code) {
		super();
		this.setMess(mess);
		this.setCode(code);
		this.date = LocalDateTime.now();
	}

	public Pacchetto(Pacchetto ogg){
		this.setMess(ogg.getMess());
		this.setCode(ogg.getCode());
		this.date = ogg.getDate();
	}

	public String getMess() {
		return mess;
	}
	public void setMess(String mess) {
		this.mess = mess;
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public boolean equals(Object obj) {
		if(obj.getClass()!=this.getClass()) return false;
		Pacchetto p=(Pacchetto)obj;
		if(p.getMess().equals(this.getMess()) && p.getCode()==this.getCode() && p.getDate().equals(this.getDate()))return true;
		return false;
	}

	@Override
	public String toString() {
		return "{"+ mess + ";" + code + ";" + date + "}";
	}
}
