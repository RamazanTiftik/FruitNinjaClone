package com.atilsamancioglu.fruitninjastarter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FruitNinja extends ApplicationAdapter implements InputProcessor {

	Random random=new Random();
	Array<Fruit> fruitArray=new Array<Fruit>(17);

	//textures
	SpriteBatch batch;
	Texture background;
	Texture apple;
	Texture bill;
	Texture cherry;
	Texture ruby;

	//sound effects
	Sound rubySound;
	Sound sliceSound;
	Sound gameStart;
	Sound gameOverSound;

	//fonts
	BitmapFont font;
	FreeTypeFontGenerator fontGen;

	//Score & Lives
	int lives=0;
	int score=0;

	//Generator & Variables
	float genCounter=0;
	private final float startGenSpeed=1.1f;
	float genSpeed=startGenSpeed;

	//time control
	private double currentTime;
	private double gameOverTime=-1.0f;

	@Override
	public void create () {

		//textures
		batch = new SpriteBatch();
		background = new Texture("ninjabackground.png");
		apple=new Texture("apple.png");
		bill=new Texture("bill.png");
		cherry=new Texture("cherry.png");
		ruby=new Texture("ruby.png");

		//sound effects
		rubySound=Gdx.audio.newSound(Gdx.files.internal("rubySound.wav"));
		sliceSound=Gdx.audio.newSound(Gdx.files.internal("slice.wav"));
		gameStart=Gdx.audio.newSound(Gdx.files.internal("gameStart.wav"));
		gameOverSound=Gdx.audio.newSound(Gdx.files.internal("gameOver.wav"));


		//fonts
		fontGen=new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameters=new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameters.color= Color.WHITE;
		parameters.size=80;
		parameters.characters="Score: 01234567890CutPlay";
		font=fontGen.generateFont(parameters);

		//fruit radius
		Fruit.radius=Math.max(Gdx.graphics.getWidth(),Gdx.graphics.getHeight())/20f;

		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		batch.begin();

		//background
		batch.draw(background,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		font.draw(batch,"Score: "+score,30,70);

		//frame per second(fps) -->calculating
		double newTime= TimeUtils.millis()/1000.0;
		System.out.println("new time: "+newTime);
		double frameTime=Math.min(newTime-currentTime,0.3);
		System.out.println("frame time: "+frameTime);
		float deltaTime=(float) frameTime;
		System.out.println("delta time: "+deltaTime);
		currentTime=newTime;

		if(lives<=0 && gameOverTime==0f){
			//game over
			gameOverTime=currentTime;
		}
		else if(lives>0){
			//game mode

			genSpeed-=deltaTime*0.01f;
			//System.out.println("genspeed: "+genSpeed);
			//System.out.println("gencounter: "+genCounter);

			//control to spawn of fruits
			if(genCounter<=0f){
				genCounter=genSpeed;
				addItem();
			} else {
				genCounter-=deltaTime;
			}

			// shown of lives
			for(int i=0;i<lives;i++){
				batch.draw(apple,i*50f+20f,Gdx.graphics.getHeight()-45f,40f,40f);
			}

			//type & draw of fruits
			for(Fruit fruit : fruitArray){
				fruit.update(deltaTime);

				switch (fruit.type){
					case REGULAR:
						batch.draw(apple,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case EXTRA:
						batch.draw(cherry,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case ENEMY:
						batch.draw(ruby,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case LIFE:
						batch.draw(bill,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
				}

			}

			//control to out of screen
			boolean holdLives=false;
			Array<Fruit> toRemove=new Array<Fruit>();
			for(Fruit fruit : fruitArray){
				if(fruit.outOfScreen()){
					toRemove.add(fruit);

					if(fruit.living && fruit.type==Fruit.Type.REGULAR){
						lives--;
						holdLives=true;
						break;
					}

				}
			}
			if(holdLives){
				for(Fruit fruit : fruitArray){
					fruit.living=false;
				}
			}
			for(Fruit fruit : toRemove){
				fruitArray.removeValue(fruit,true);
			}

		}

		//game over screen
		if(lives<=0){
			//sound effect
			long id2=gameOverSound.play(1.0f);
			gameOverSound.setPitch(id2,1);
			gameOverSound.setLooping(id2,false);

			font.draw(batch,"Cut to Play!",Gdx.graphics.getWidth()/3+Gdx.graphics.getWidth()/12,Gdx.graphics.getHeight()/2);
		}

		batch.end();
	}

	public void addItem(){

		float pos=random.nextFloat()*Gdx.graphics.getWidth(); // random position

		//Item's position & velocity
		float fruitVelocityY=Gdx.graphics.getHeight()*0.5f;
		float fruitVelocityX=(Gdx.graphics.getWidth() * 0.5f - pos) * (0.3f + (random.nextFloat() - 0.5f));
		Fruit item=new Fruit(new Vector2(pos,-Fruit.radius),new Vector2(fruitVelocityX,fruitVelocityY));

		//control of difficult algorithms
		float type=random.nextFloat();
		if(type>=0.98){
			item.type=Fruit.Type.LIFE;
		} else if (type>=0.85) {
			item.type=Fruit.Type.EXTRA;
		} else if (type>=0.72) {
			item.type=Fruit.Type.ENEMY;
		}

		fruitArray.add(item);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		fontGen.dispose();
		sliceSound.dispose();
		gameOverSound.dispose();
		rubySound.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		if(lives<=0 && currentTime-gameOverTime>2f){
			//menu mode
			gameOverTime=0f;
			score=0;
			lives=5;
			genSpeed=startGenSpeed;
			fruitArray.clear();

		} else {
			//game mode

			Array<Fruit> toRemove=new Array<Fruit>();
			Vector2 pos=new Vector2(screenX,Gdx.graphics.getHeight()-screenY);

			for(Fruit fruit : fruitArray){
				if(fruit.clicked(pos)){
					toRemove.add(fruit);

					switch (fruit.type){

						case REGULAR:
							//sound effect
							long id=sliceSound.play(1.0f);
							sliceSound.setPitch(id,1);
							sliceSound.setLooping(id,false);

							score++;
							break;
						case EXTRA:
							//sound effect
							long id1=sliceSound.play(1.0f);
							sliceSound.setPitch(id1,1);
							sliceSound.setLooping(id1,false);

							score+=3;
							break;
						case ENEMY:
							//sound effect
							long id2=rubySound.play(1.0f);
							rubySound.setPitch(id2,1);
							rubySound.setLooping(id2,false);

							lives--;
							break;
						case LIFE:
							//sound effect
							long id3=sliceSound.play(1.0f);
							sliceSound.setPitch(id3,1);
							sliceSound.setLooping(id3,false);

							lives++;
							break;

					}

				}
			}

			for(Fruit fruit : toRemove){
				fruitArray.removeValue(fruit,true);
			}

		}

		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
