package ameshforecast;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Blob;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Gifdat {
	@PrimaryKey
	@Persistent
	private String filename;
	@Persistent
	private Blob content;

	public Gifdat(String filename, Blob content) {
		this.filename = filename;
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public Blob getContent() {
		return content;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setContent(Blob content) {
		this.content = content;
	}
}
