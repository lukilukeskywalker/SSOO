//Proyecto SETI por Lukas Gdanietz y Manuel Infantes
enum State{
		NEW,
		IN_PROGRESS, 
		DONE,
		FAILING
	}
public class Task_Activo extends Task{
	public int resultado;
	public State status;
	public int voluntarios_act;
	public int sub_id=-1;

	/**
	*Constructor Genera una tarea activa
	*@param Task Tarea padre
	*/
	public Task_Activo(Task task) {
		super(task.id, task.data);
	}
	/**
	*Constructor Genera una tarea activa
	*@param Task Tarea padre
	*@param sub_id sub id para identificar que voluntario a procesado la tarea
	*/
	public Task_Activo(Task task, int sub_id) {
		super(task.id, task.data);
		this.sub_id=sub_id;
	}
	public String toString() {
		if(this.status == State.DONE)return " TaskID: " + id + " Dato: " +data+ " Resultado: "+ resultado +" Estado: "+ status;
		return "SUB_ID"+ sub_id +"TaskID: " + id + " Dato: " +data+ " Estado: "+ this.status;
	}
	public Task parent() {
		Task oldTask = new Task(this.id, this.data);
		return oldTask;
	}


}