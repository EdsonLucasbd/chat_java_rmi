package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota para classes de clientes
 * Um método para receber uma string
 * Um método para atualizar as alterações na lista de usuários
 * 
 * @author Edson e Jorge
 *
 */
public interface ChatClient3IF extends Remote{

	public void messageFromServer(String message) throws RemoteException;

	public void updateUserList(String[] currentUsers) throws RemoteException;
	
}
