package app.registrartransacao;

public record Transacao(int idCliente, long valor, String tipo, String descricao) {

    public long valorAbsoluto(){
        return this.valor;
    }
    @Override
    public long valor() {
        if(tipo.equals("d")){
            return valor*-1;
        }
        return valor;
    }
}
