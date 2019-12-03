/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projects.wsn1.nodes.nodeImplementations;

import java.util.Random;
import projects.wsn1.nodes.messages.WsnMsg;
import projects.wsn1.nodes.timers.WsnMessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

/**
 *
 * @author pozza
 */
public class SimpleNode extends Node {
   
    private final Integer INICIO_INTERVALO = 100;
    private final Integer FIM_INTERVALO    = 200;
    private Node proximoNoAteEstacaoBase;
    private Integer sequenceNumber = 0;
    private boolean enviando = true;

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            
            Message message = inbox.next();
            if (message instanceof WsnMsg) {
                Boolean encaminhar = Boolean.TRUE;
                WsnMsg wsnMessage = (WsnMsg) message;                
                if (wsnMessage.forwardingHop.equals(this)) { // A mensagem voltou. O no deve descarta-la
                    encaminhar = Boolean.FALSE;
                } else if (wsnMessage.tipoMsg == 0) { // A mensagem é um flood. Devemos atualizar a rota
                    if (proximoNoAteEstacaoBase == null) {
                        System.out.println("O noh "+this.ID+" esta recebendo dados de Sink");
                        proximoNoAteEstacaoBase = inbox.getSender();
                        sequenceNumber = wsnMessage.sequenceID;
                    } else if (sequenceNumber < wsnMessage.sequenceID) {
                    //Recurso simples para evitar loop.
                        //Exemplo: Noh A transmite em brodcast. Noh B recebe a
                        //msg e retransmite em broadcast.
                        //Consequentemente, noh A irá receber a msg. Sem esse
                        //condicional, noh A iria retransmitir novamente, gerando um loop
                        sequenceNumber = wsnMessage.sequenceID;
                    } else {
                        encaminhar = Boolean.FALSE;
                    }
                } else if (wsnMessage.tipoMsg == 1) {
                    sendDirect(wsnMessage, proximoNoAteEstacaoBase);
                    wsnMessage.forwardingHop = this;
                    encaminhar = Boolean.FALSE;                   
                                              
                }
                if (encaminhar) {
                    //Devemos alterar o campo forwardingHop(da mensagem) para armazenar o
//noh que vai encaminhar a mensagem.
                    wsnMessage.forwardingHop = this;
                    broadcast(wsnMessage);
                }
            }
        }
    }
    
     public void setEnviando(Boolean enviando) {
        this.enviando = enviando;
    }

    public Node proximoNoAteEstacaoBase() {
        return this.proximoNoAteEstacaoBase;
    }

    @Override
    public void preStep() {
        if(proximoNoAteEstacaoBase != null && enviando) {
    	   WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 1);
           WsnMessageTimer wsnMessageTimer = new WsnMessageTimer(wsnMessage);
           wsnMessageTimer.startRelative(new Random().nextInt(INICIO_INTERVALO) + (FIM_INTERVALO - INICIO_INTERVALO), this);
           enviando = false;
       }
    }

//    @NodePopupMethod(menuText = "Construir Arvore de Roteamento")
//    public void construirRoteamento() {
//        this.proximoNoAteEstacaoBase = this;
//        WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0);
//        WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
//        timer.startRelative(1, this);
//    }

    @Override
    public void init() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void neighborhoodChange() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void postStep() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
