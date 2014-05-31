package archstudio.filesys;

import java.util.Properties;

public class RepositoryMetadata implements java.io.Serializable{
	protected String rootPath;
	protected String repositoryID;
	protected String repositoryType;
	protected Properties properties;

	public RepositoryMetadata(String rootPath, String repositoryID, 
	String repositoryType, Properties properties){
		this.rootPath = rootPath;
		this.repositoryID = repositoryID;
		this.repositoryType = repositoryType;
		this.properties = properties;
	}

	public Properties getProperties(){
		return properties;
	}

	public String getRepositoryID(){
		return repositoryID;
	}

	public String getRootPath(){
		return rootPath;
	}
}
