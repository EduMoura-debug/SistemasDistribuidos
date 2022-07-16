import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;


public class Processo {

    private int pid;
    private boolean ehCoordenador = false;
    private Thread utilizaRecurso = new Thread();
    private Conexao conexao = new Conexao();

    private LinkedList<Processo> listaDeEspera;
    private boolean recursoEmUso;

    private static final int USO_PROCESSO_MIN=10000;
    private static final int USO_PROCESSO_MAX=20000;
   
    public Processo(int pid){
        this.pid=pid;
        setEhCoordenador(false);
    }

    public int getPid(){
        return pid;
    }

    public boolean isCoordenador(){
        return ehCoordenador;
    }

    public void setEhCoordenador(boolean ehCoordenador){
        this.ehCoordenador = ehCoordenador;
        if(this.ehCoordenador){
            listaDeEspera = new LinkedList<>();
            conexao.conectar(this);

            if(ControladorDeProcessos.isSendoConsumido())
                ControladorDeProcessos.getConsumidor().interromperAcessoRecurso();
        }    
    }

    private void interromperAcessoRecurso(){
        if(utilizaRecurso.isAlive())
            utilizaRecurso.interrupt();
    }

    public boolean isRecursoEmUso() {
        return encontrarCoordenador().recursoEmUso;
    }

    public void setRecursoEmUso( boolean estaEmUso, Processo consumidor){
        Processo coordenador = encontrarCoordenador();

        coordenador.recursoEmUso = estaEmUso;
        ControladorDeProcessos.setConsumidor(estaEmUso ? consumidor : null);
    }

    private LinkedList<Processo> getListaDeEspera(){
        return encontrarCoordenador().listaDeEspera;
    }

    private boolean isListaDeEsperaVazia(){
        return getListaDeEspera().isEmpty();
    }

    private void removerDaListaDeEspera(Processo processo){
        if(getListaDeEspera().contains(processo))
            getListaDeEspera().remove(processo);
    }

    private Processo encontrarCoordenador(){
        Processo coordenador = ControladorDeProcessos.getCoordenador();
        
        if(coordenador == null) {
            ArrayList<Processo> processosAtivos =new ArrayList<Processo>();
            processosAtivos = ControladorDeProcessos.getProcessosAtivos();
            int pid = this.getPid();
            coordenador = realizarEleicao(processosAtivos, pid);
        }
        return coordenador;
    }

    public void acessarRecursoCompartilhado() {
        if(ControladorDeProcessos.isUsandoRecurso(this) || this.isCoordenador())
            return;
        
        String resultado = conexao.realizarRequisicao("Processo " + this + " quer consumir o recurso.\n");
        System.out.println("Resultado da requisicao do processo " + this + ": " + resultado);

        if(resultado.equals(Conexao.PERMITIR_ACESSO)){
            utilizarRecurso(this);
        }else if(resultado.equals(Conexao.NEGAR_ACESSO)){
            adicionarNaListaDeEspera(this);
        }

    }

    private void adicionarNaListaDeEspera(Processo processoEmEspera){
        getListaDeEspera().add(processoEmEspera);
        System.out.println("Processo "+ this + " foi adicionado na lista de espera.");
        System.out.println("Lista de espera: " + getListaDeEspera());
    }

    private void utilizarRecurso(Processo processo) {
        Random random = new Random();
        int randomUsageTime = USO_PROCESSO_MAX + random.nextInt(USO_PROCESSO_MAX - USO_PROCESSO_MIN);
        utilizaRecurso = new Thread(new Runnable() {
            @Override
            public void run(){
                System.out.println("Processo " + processo + " está consumindo o recurso.");
                setRecursoEmUso(true, processo);

                try{
                    Thread.sleep(randomUsageTime);
                }catch(InterruptedException e) { }

                System.out.println("Processo " + processo + " Parou de consumir o recurso.");
            }   
        });
        utilizaRecurso.start();
    }

    private void liberarRecurso() {
        setRecursoEmUso(false, this);
        if(!isListaDeEsperaVazia()){
            Processo processoEmEspera = getListaDeEspera().removeFirst();
            processoEmEspera.acessarRecursoCompartilhado();
            System.out.println("Processo " + processoEmEspera + " foi removido da lista de espera.");
            System.out.println("Lista de Espera: " + getListaDeEspera());
        }
    }

    public void destruir(){
        if(isCoordenador()){
            conexao.encerrarConcexao();
        }else{
            removerDaListaDeEspera(this);
            if(ControladorDeProcessos.isUsandoRecurso(this)){
                interromperAcessoRecurso();
                liberarRecurso();
            }
        }

        ControladorDeProcessos.removerProcesso(this);
    }

    private static Processo realizarEleicao(ArrayList<Processo> processosAtivos, int pid){
        
        Processo coordenador = new Processo(pid);
        LinkedList<Integer> idProcessosConsultados = new LinkedList<>();
        for (Processo p : ControladorDeProcessos.getProcessosAtivos()){
            p.consultarProcesso(idProcessosConsultados);
        }
        
        int idNovoCoordenador = pid;
        for (Integer id : idProcessosConsultados){
            if(id > idNovoCoordenador){
                idNovoCoordenador = id;
            }
        }

        for(Processo p : processosAtivos){
            if(p.getPid() == idNovoCoordenador){
                p.setEhCoordenador(true);
                System.out.println("Eleicao concluida com sucesso. Novo coordenador: " + idNovoCoordenador);
                coordenador = p;
                break;
            }else{
                p.setEhCoordenador(false);
            }
        }
        return coordenador;
    }

    private void consultarProcesso(LinkedList<Integer> processosConsultados){
        processosConsultados.add(getPid());
    }

    @Override
    public boolean equals(Object objeto){
        Processo processo = (Processo) objeto;
        if(processo == null)
            return false;
        return this.pid == processo.pid;
    }

    @Override
    public String toString(){
        return String.valueOf(this.getPid());
    }
}

