package archstudio.filesys;

public interface IFileSystem{

	public void createRepository(String repositoryID, String repositoryType, Object creationParams);
	public void createRepository(String repositoryID);
	public void removeRepository(String repositoryID);
	
	public String[] getAllRepositoryIDs();
	public RepositoryMetadata getRepositoryMetadata(String repositoryID);
	public RepositoryMetadata[] getAllRepositoryMetadata();
	
	public void setProperty(String repositoryID, String propertyName, String value);
	public String getProperty(String repositoryID, String propertyName);
	
	public void mkdir(String repositoryID, String path);
	public boolean exists(String repositoryID, String path);
	public boolean canRead(String repositoryID, String path);
	public boolean canWrite(String repositoryID, String path);
	
	public void writeFile(String repositoryID, String path, byte[] contents);
	public void writeFile(String repositoryID, String path, String contents);
	//Writes as serialized XML
	public void writeFile(String repositoryID, String path, java.io.Serializable[] contents);
	
	public byte[] readFileAsByteArray(String repositoryID, String path);
	public String readFileAsText(String repositoryID, String path);
	public Object[] readFileAsObjectStore(String repositoryID, String path);
	
	public String[] list(String repositoryID, String path);
	public String[] listFiles(String repositoryID, String path);
	public String[] listDirectories(String repositoryID, String path);
	
	public void rename(String repositoryID, String path, String newName);
	public void delete(String repositoryID, String path);
	
	public void deleteAll(String repositoryID);
	public void deleteAll(String repositoryID, String path);
	
}
