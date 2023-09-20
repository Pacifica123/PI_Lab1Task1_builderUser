package org.example;

public class User {
    private String name;
    private Integer age;
    private String email;

    private User(Builder builder){
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Самовозвращающие методы для Fluent Interface и цепочки вызовов (читабельность)
     */
    public static class Builder{
        private String name;
        private Integer age;
        private String email;


        public Builder(){
            // Значения по умолчанию:
            name = "Nameless";
            email = "NONE";
            age = 0;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setAge(Integer age) {
            this.age = age;
            return this;
        }
        public Builder setEmail(String email){
            this.email = email;
            return this;
        }
        public User build(){
            return new User(this);
        }
    }
}
